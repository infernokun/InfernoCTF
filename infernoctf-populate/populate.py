#!/usr/bin/python3

import os
import sys

import requests

import docker_challenge

url = os.environ.get("INFERNOCTF_API", "http://127.0.0.1:8081/infernoctf-rest/api")
admin_user = os.environ.get("INFERNOCTF_ADMIN_USER", "infernoctf_admin")
admin_password = os.environ.get("INFERNOCTF_ADMIN_PASSWORD")

# The API requires a bearer token, and creating rooms and challenges requires an authoring
# role, so log in first.
_session = requests.Session()


def login():
  if not admin_password:
    sys.exit(
        "Set INFERNOCTF_ADMIN_PASSWORD (and optionally INFERNOCTF_ADMIN_USER / INFERNOCTF_API).\n"
        "The API requires authentication; creating rooms and challenges requires an "
        "ADMIN, DEVELOPER, CREATOR or FACILITATOR account.")

  res = _session.post(url + "/auth/login",
                      json={"username": admin_user, "password": admin_password},
                      verify=False)
  if res.status_code != 200:
    sys.exit(f"Login failed ({res.status_code}): {res.text}")

  token = res.json()["data"]["jwt"]
  _session.headers.update({
      "Authorization": f"Bearer {token}",
      "Content-Type": "application/json",
      "Accept": "application/json",
  })


def get_admin_user():
  res = _session.get(url + "/user/by?username=" + admin_user, verify=False)
  res.raise_for_status()
  return res.json()['data']


def get_docker_room():
  res = _session.get(url + "/room/by?name=Docker%20Room", verify=False)
  res.raise_for_status()
  return res.json()['data']


def room_docker():
  user = get_admin_user()

  data = {
      "name": "Docker Room",
      "creator": {"id": user['id']}
  }

  res = _session.post(url + "/room", json=data, verify=False)
  res.raise_for_status()
  return res.json()


def docker_challenges():
  challenge_endpoint = url + "/ctf-entity"

  docker_room = get_docker_room()
  user = get_admin_user()

  challenges = docker_challenge.get_challenges(docker_room, user)

  for challenge in challenges:
    res = _session.post(challenge_endpoint, json=challenge, verify=False)
    print(res.json())
    if res.status_code == 200:
      print("Challenge created successfully.")


def main():
  login()
  # room_docker()
  docker_challenges()


if __name__ == '__main__':
  main()
