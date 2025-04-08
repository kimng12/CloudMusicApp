import json
import boto3
studentID = "s3970589"
def subscribe(email=None,title = None,artist=None):
    dynamodb = boto3.resource("dynamodb")
    ret = {"status":"Fail"}
    try:
        if email!=None and title!=None and artist != None :
            login = dynamodb.Table("login")
            music = dynamodb.Table("music")
            key = {'artist':artist,'title':title}
            song = music.get_item(Key=key)
            if "Item" in song:
                subscriptions = login.get_item(Key={'email':email})['Item']['subscriptions']
                if key not in subscriptions:
                    song['Item']['image_url'] = boto3.client('s3').generate_presigned_url(
                        ClientMethod='get_object',
                        Params={'Bucket': f'{studentID}-artist-images', 'Key': song['Item']['image_url'].split('/')[-1]},
                        ExpiresIn=3600
                    )
                    subscriptions.append(key)
                    login.update_item(Key={'email':email},
                                      UpdateExpression="SET subscriptions= :subscriptions",
                                      ExpressionAttributeValues={":subscriptions":subscriptions})
                    ret = {"status":"Success", "subscription":song['Item']}
            else:
                ret["status"]="NoSong"
        else:
            ret["status"]= "NoneValues"
    except Exception as e:
        raise e
    return ret

def unsubscribe(email=None,title = None,artist=None):
    dynamodb = boto3.resource("dynamodb")
    ret = False
    try:
        if email!=None and title!=None and artist != None :
            login = dynamodb.Table("login")
            song = {'artist':artist,'title':title}
            subscriptions = login.get_item(Key={'email':email})['Item']['subscriptions']
            if song in subscriptions:
                subscriptions.remove(song)
                login.update_item(Key={'email':email},
                                  UpdateExpression="SET subscriptions= :subscriptions",
                                  ExpressionAttributeValues={":subscriptions":subscriptions})
                ret = True
    except Exception as e:
        raise e
    return ret

def getPresignedURL(songs,bucketName=f"{studentID}-artist-images",URLExpiration = 3600):
    ret = []
    try:
        s3 = boto3.client('s3')
        artists = {}
        for song in songs:
            if song['artist'] not in artists:
                song['image_url'] = s3.generate_presigned_url(
                    ClientMethod='get_object',
                    Params={'Bucket': bucketName, 'Key': song['image_url'].split('/')[-1]},
                    ExpiresIn=URLExpiration
                )
                artists[song['artist']] = song['image_url']
            else:
                song['image_url'] = artists[song['artist']]
            ret.append(song)
    except Exception as e:
        print("Exception:",e)
    return ret
def search(body):
    dynamodb=boto3.client('dynamodb')
    ret = []
    title = None
    artist = None
    year = None
    if "year" in body:
        year = str(body["year"])
        year = str(body["year"])
    if "title" in body:
        title = body["title"]
    if "artist" in body:
        artist = body["artist"]
    if artist or title or year:
        searchAttributes = []
        searchValues = {}
        attributeNames ={}
        if title:
            searchAttributes.append("#title=:title")
            searchValues[":title"] = {'S':title}
            attributeNames["#title"] = "title"
        if year:
            searchAttributes.append(" #year =:year")
            searchValues[":year"] = {'N': year}
            attributeNames["#year"] = "year"
        if artist:
            searchAttributes.append("#artist=:artist")
            searchValues[":artist"] = {'S':artist}
            attributeNames["#artist"] = "artist"
        filterExpression = " AND  ".join(searchAttributes) if searchAttributes else None
        response = dynamodb.scan(
            TableName='music',
            FilterExpression=filterExpression,
            ExpressionAttributeValues=searchValues,
            ExpressionAttributeNames=attributeNames
        )
        for i,song in enumerate(response['Items']):
            for attribute in song:
                if 'S' in song[attribute]:
                    song[attribute] = song[attribute]['S']
                elif 'N' in song[attribute]:
                    song[attribute] = song[attribute]['N']
            response['Items'][i] = song
        ret = getPresignedURL(response['Items'])
    return ret

def lambda_handler(event, context):
    call = event['call']
    try:
        statusCode = 200
        if call == 'subscribe':
            response = subscribe(email=event['email'],artist=event['artist'],title=event['title'])
            if response['status'] == "Success":
                response['message']=f'The song "{response['subscription']['title']}" was successfuly added.'
            elif response['status'] == "NoSong":
                response['message'] = f'The song {event['title']} does not exist in our library.'
            elif response['status'] == "NoneValues":
                response['message'] = f'Please make sure to inclued all the values necessary.'
            elif response['status'] =="Fail":
                response['message']=f'The song is already subscribed'
            return {
                'statusCode': statusCode,
                'body': response
            }
        elif call == 'unsubscribe':

            if unsubscribe(email=event['email'],artist=event['artist'],title=event['title']):
                message = {'status':'Success','message':f'The song {event['title']} has been unsubscribe for {event['email']}.'}
            else:
                message = {'status':'Fail','message':f'The song was not subscribed'}
            return {
                'statusCode': statusCode,
                'body': message
            }
        elif call == 'search':
            search(event)

            statusCode = 200
            return {
                'statusCode': statusCode,
                'body': {'status':'Success','songs':search(event)}
            }
        else:
            return {
                'statusCode': 404,
                'body': {'status':'Invalid request type.'}
            }
    except Exception as e:
        print(e)
        return {
            'statusCode': 500,
            'body': {'status':'Internal server error.','message':str(e),'event':event}
        }