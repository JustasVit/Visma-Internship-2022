# Visma-Internship-2022
Java developer task for Visma internship 2022

## Security
Endpoints are secured using JWT token. In order to access endpoints, register a new user in ```localhost:8080/api/auth/register``` or login in ```localhost:8080/api/auth/login```

## Endpoints

```localhost:8080/api/meetings (POST)``` is used for creating a new meeting.

```localhost:8080/api/meetings/{id} (DELETE)``` is used for deleting a meeting based on it's id.

```localhost:8080/api/meetings/filter (POST)``` is used for getting a list of filtered meetings.

```localhost:8080/api/meetings/user (POST)``` is used for adding a user to a specified meeting at specified time.

```localhost:8080/api/meetings/user (DELETE)``` is used for removing a user from specified meeting.
