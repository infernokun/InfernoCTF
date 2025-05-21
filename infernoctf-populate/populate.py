from enum import Flag
import requests
from datetime import datetime, timedelta

url = "http://127.0.0.1:8081/infernoctf-rest/api"
admin_user = "infernoctf_admin"

def get_admin_user():
  res = requests.get(url + "/user?username=" + admin_user, verify=False)
  
  return res.json()

def get_docker_room():
  res = requests.get(url + "/room?name=Docker Room", verify=False)
  
  return res.json()

def room_docker():
  user = get_admin_user()
  
  room_endpoint = url + "/room"
  
  headers = {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
  
  data = {
        "name": "Docker Room",
        "creator": {"id": user['id']}
    }
  
  res = requests.post(room_endpoint, headers=headers, json=data, verify=False)
  
  return print(res.json())

def docker_challenges():
  challenge_endpoint = url + "/ctf-entity"
  
  docker_room = get_docker_room()
  
  headers = {
        "Content-Type": "application/json",
        "Accept": "application/json"
  }
  
  challenges = [
        {
            "question": "What is the command to installed Docker containers?",
            "maxAttempts": 3,
            "room": { "id": docker_room['id'] },
            "description": "This challenge tests your knowledge of Docker commands.",
            "hints": ["Check the Docker documentation.", "Remember the command starts with 'docker'."],
            "flags": [
              {
                "flag": "docker container ls",
                "surroundWithTag": False,
                "caseSensitive": False,
                "weight": 1
                }
              ],
            "category": "Docker Commands",
            "difficultyLevel": "Easy",
            "points": 10,
            "author": get_admin_user()['username'],  # Assuming username is available
            "tags": ["docker", "commands"],
            "visible": True,
            "expirationDate": (datetime.now() + timedelta(days=30)).strftime('%Y-%m-%d %H:%M:%S'),
            "attachments": [],
            "solutionExplanation": "The command is 'docker container ls'.",
            "relatedChallengeIds": []
        },
        {
            "question": "How do you build a Docker image from a Dockerfile?",
            "maxAttempts": 2,
            "room": {"id": docker_room['id']},  # Replace with the actual room ID
            "description": "This challenge tests your knowledge of building Docker images.",
            "hints": ["Look for the build command in the Docker CLI.", "Make sure to use a proper Dockerfile."],
            "flags": [
              {
                "flag": "docker build",
                "surroundWithTag": False,
                "caseSensitive": False,
                "weight": 1
                }
              ],
            "category": "Docker",
            "difficultyLevel": "Medium",
            "points": 15,
            "author": get_admin_user()['username'],
            "tags": ["docker", "images"],
            "visible": True,
            "expirationDate": (datetime.now() + timedelta(days=30)).strftime('%Y-%m-%d %H:%M:%S'),
            "attachments": [],
            "solutionExplanation": "The command is 'docker build -t <image-name> .'.",
            "relatedChallengeIds": []
        }
    ]

  # Post each challenge
  for challenge in challenges:
    res = requests.post(challenge_endpoint, headers=headers, json=challenge, verify=False)
    if res.status_code == 201:
      print("Challenge created successfully.")

def main():
  #room_docker()
  docker_challenges()
  

if __name__ == '__main__':
  main()