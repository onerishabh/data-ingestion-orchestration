import json
import re

def check_email(email: str):
    regex = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
    return re.fullmatch(regex, email)

def lambda_handler(event, context):
    if 'user_name' in event and 'email' in event and 'pincode' in event and check_email(event['email']):
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
