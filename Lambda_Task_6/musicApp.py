import json
import boto3

STUDENTID = "s4027383"

def respond(status_code, body):
    return {
        "statusCode": status_code,
        "headers": {
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "*",
            "Access-Control-Allow-Headers": "*"
        },
        "body": json.dumps(body)
    }

def getPresignedURL(songs, bucketName=f"{STUDENTID}-a1-s3-bucket", URLExpiration=3600):
    s3 = boto3.client('s3')
    seen = {}
    for song in songs:
        key = f"artists/{song.get('image_url', '').split('/')[-1]}"
        if song['artist'] not in seen:
            song['image_url'] = s3.generate_presigned_url(
                ClientMethod='get_object',
                Params={'Bucket': bucketName, 'Key': key},
                ExpiresIn=URLExpiration
            )
            seen[song['artist']] = song['image_url']
        else:
            song['image_url'] = seen[song['artist']]
    return songs

def login(event):
    email = event.get("email")
    password = event.get("password")
    if not email or not password:
        return respond(400, {"status": "Missing credentials"})

    try:
        table = boto3.resource("dynamodb").Table("login")
        user = table.get_item(Key={"email": email}).get("Item")

        if not user or user["password"] != password:
            return respond(200, {"status": "Incorrect"})

        subscriptions = user.get("subscriptions", [])
        songs = []

        if subscriptions:
            music_table = boto3.resource("dynamodb").Table("music")
            keys = [{"artist": s["artist"], "title": s["title"]} for s in subscriptions]
            for i in range(0, len(keys), 25):
                batch = keys[i:i+25]
                res = music_table.meta.client.batch_get_item(RequestItems={"music": {"Keys": batch}})
                songs.extend(res["Responses"]["music"])
            songs = getPresignedURL(songs)

        return respond(200, {
            "status": "Success",
            "user": {
                "email": user["email"],
                "user_name": user["user_name"]
            },
            "subscriptions": songs
        })

    except Exception as e:
        return respond(500, {"status": "Error", "message": str(e)})

def register(event):
    email = event.get("email")
    password = event.get("password")
    user_name = event.get("user_name")

    if not email or not password or not user_name:
        return respond(400, {"status": "Missing required fields"})

    try:
        dynamodb = boto3.resource("dynamodb")
        table = dynamodb.Table("login")
        table.put_item(
            Item={
                "email": email,
                "password": password,
                "user_name": user_name,
                "subscriptions": []
            },
            ConditionExpression="attribute_not_exists(email)"
        )
        return respond(200, {"status": "Succesfully registered"})

    except Exception as e:
        if getattr(e, "response", {}).get("Error", {}).get("Code") == "ConditionalCheckFailedException":
            return respond(200, {"status": "Email already exists"})
        return respond(500, {"status": "Error", "message": str(e)})

def subscribe(event):
    email = event.get("email")
    title = event.get("title")
    artist = event.get("artist")
    if not email or not title or not artist:
        return respond(400, {"status": "Missing fields"})

    dynamodb = boto3.resource("dynamodb")
    login_table = dynamodb.Table("login")
    music_table = dynamodb.Table("music")

    key = {"title": title, "artist": artist}
    music_res = music_table.get_item(Key=key)
    if "Item" not in music_res:
        return respond(200, {"status": "NoSong"})

    login_res = login_table.get_item(Key={"email": email})
    subs = login_res["Item"].get("subscriptions", [])
    if key in subs:
        return respond(200, {"status": "Fail", "message": "Already subscribed"})

    subs.append(key)
    login_table.update_item(
        Key={"email": email},
        UpdateExpression="SET subscriptions = :s",
        ExpressionAttributeValues={":s": subs}
    )

    song = music_res["Item"]
    song['image_url'] = getPresignedURL([song])[0]['image_url']
    return respond(200, {"status": "Success", "message": f"Subscribed to {title}", "subscription": song})

def unsubscribe(event):
    email = event.get("email")
    title = event.get("title")
    artist = event.get("artist")
    if not email or not title or not artist:
        return respond(400, {"status": "Missing fields"})

    dynamodb = boto3.resource("dynamodb")
    table = dynamodb.Table("login")

    user = table.get_item(Key={"email": email})
    subs = user["Item"].get("subscriptions", [])
    song = {"title": title, "artist": artist}
    if song in subs:
        subs.remove(song)
        table.update_item(
            Key={"email": email},
            UpdateExpression="SET subscriptions = :s",
            ExpressionAttributeValues={":s": subs}
        )
        return respond(200, {"status": "Success", "message": f"Unsubscribed from {title}"})
    else:
        return respond(200, {"status": "Fail", "message": "Song was not subscribed"})

def search(event):
    title = event.get("title", "").strip()
    artist = event.get("artist", "").strip()
    album = event.get("album", "").strip()
    year = event.get("year", "").strip()

    if not any([title, artist, album, year]):
        return respond(400, {"status": "Empty query"})

    dynamodb = boto3.client("dynamodb")
    filter_exp = []
    values = {}
    names = {}

    if title:
        filter_exp.append("#t = :t")
        values[":t"] = {"S": title}
        names["#t"] = "title"
    if artist:
        filter_exp.append("#a = :a")
        values[":a"] = {"S": artist}
        names["#a"] = "artist"
    if album:
        filter_exp.append("#al = :al")
        values[":al"] = {"S": album}
        names["#al"] = "album"
    if year:
        filter_exp.append("#y = :y")
        values[":y"] = {"S": year}
        names["#y"] = "year"

    response = dynamodb.scan(
        TableName="music",
        FilterExpression=" AND ".join(filter_exp),
        ExpressionAttributeNames=names,
        ExpressionAttributeValues=values
    )

    songs = []
    for item in response["Items"]:
        song = {k: (v.get("S") or v.get("N")) for k, v in item.items()}
        songs.append(song)

    songs = getPresignedURL(songs)
    return respond(200, {"status": "Success", "songs": songs})

def lambda_handler(event, context):
    call = event.get("call")

    if call == "login":
        return login(event)
    elif call == "register":
        return register(event)
    elif call == "subscribe":
        return subscribe(event)
    elif call == "unsubscribe":
        return unsubscribe(event)
    elif call == "search":
        return search(event)
    else:
        return respond(404, {"status": "Unknown call"})
