import json

def check_pincode(pincode: str):
    return len(pincode) in (4,6) and pincode.isdigit()

def lambda_handler(event, context):
    print(event)
    if 'user_name' in event and 'email' in event and 'pincode' in event and check_pincode(event['pincode']):
        return {
            'statusCode': 200,
            'body': {'user_name':event['user_name'].upper(), 
                'email' : event['email'],
                'pincode' : event['pincode']
            }
        }
    else:
        return {
            'statusCode': 400,
            'body': json.dumps('FAILED')
        }
