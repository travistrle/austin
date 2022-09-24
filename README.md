# austin

a monolith backend for onegrid

## Getting started for Developer

### Setup PostgresSQL database
```bash
# Step 1: Install postgres version 11, which is the version we currently use in production:
$ brew update
$ brew install postgresql@11
$ brew services start postgres

# Step 2: Create the 'postgres' superuser:
$ createuser -s postgres

# Step 3: Create database & user
$ psql -U postgres
postgres=# create database austin;
postgres=# create user austinuser with encrypted password 'austinpass';
postgres=# grant all privileges on database austin to austinuser;
```

### Start service
```bash
npm install
./gradlew -x webapp
npm start

npm install
npm run webapp:build 
./gradlew
```
