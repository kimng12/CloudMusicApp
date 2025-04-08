import json
import boto3
STUDENTID = "s3946837"
def getBatches(subscriptions=None,batchSize=50):
  ret = []
  for i in range(0,len(subscriptions),batchSize):
    ret.append(subscriptions[i:i+batchSize])
  return ret

def getPresignedURL(songs,bucketName=f"{STUDENTID}-artist-images",URLExpiration = 3600):
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

def getSubscriptionArtists(subscriptions=None):
  dynamodb = boto3.resource("dynamodb")
  ret = []
  if dynamodb!=None and subscriptions !=None:
    try:
      for batch in getBatches(subscriptions):
        response = dynamodb.batch_get_item(RequestItems={'music': {'Keys': batch}})
        songs = getPresignedURL(response['Responses']['music'])
        ret += songs
    except Exception as e:
      print("Exception: ",e)
  return ret

def login(body):
  email = None
  password = None
  if "email" in body:
    email = body["email"]
  if "password" in body:
    password = body["password"] 
  
  ret = {'status':'Fail'}
  if email !=None and password !=None:
    try:
      dynamodb = boto3.resource("dynamodb")
      table = dynamodb.Table("login")
      response = table.get_item(Key={"email":email})
      if 'Item' in response and password == response["Item"]["password"]:
        print(response['Item']['subscriptions'])
        response['Item']['subscriptions'] = getSubscriptionArtists(response['Item']['subscriptions'])
        ret =  {'status':'Success','subscriptions':response['Item']['subscriptions'],'user':{'email':response['Item']['email'],'user_name':response['Item']['user_name']}}
      else:
        ret = {'status':'Incorrect'}
    except Exception as e:
      raise e
  return ret

def register(body):
    email = None
    password = None
    user_name = None
    if "email" in body:
      email = str(body["email"])
    if "password" in body:
      password = body["password"] 
    if "user_name" in body:
      user_name = body["user_name"] 
    
    ret = {"status" : "Could not register user"}
    if email!=None and user_name!=None and password!=None:
      try:
        dynamodb = boto3.resource("dynamodb")
        table = dynamodb.Table("login")
        response =  table.put_item( 
              Item={
              'email':email, 
              'user_name':user_name, 
              'password': password,
              'subscriptions':[]
            },
              ConditionExpression="attribute_not_exists(email)")
        ret = f'Succesfully registered'
      except Exception as e:
        if e.response['Error']['Code'] == 'ConditionalCheckFailedException':
            ret = {"status" : "Email already exists"}
        else:
            raise e
    return ret

def lambda_handler(event, context):
    call = event['call']
    try:
      message = ''
      statusCode = 200
      if call == 'login':
        message = login(event)
        return {
            'statusCode': statusCode,
            'body': message
        }
      elif call == 'register':
        message = register(event)
        statusCode = 200
        return {
            'statusCode': statusCode,
            'body': message
        }
      else:
        return {
            'statusCode': 404,
            'body': json.dumps('Invalid request type.')
        }
    except Exception as e:
      print(e)
      return {
            'statusCode': 500,
            'body': json.dumps('Internal server error.')
        }
      
