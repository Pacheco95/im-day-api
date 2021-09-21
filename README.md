# im-day-api

```shell
git clone https://github.com/Pacheco95/im-day-api.git
cd im-day-api
```

## Run tests

```shell
# Setup environment variables
POSTGRES_CONTAINER_NAME=postgres-imday-test
EXPORTED_POSTGRES_PORT=9999
DB_PORT=$EXPORTED_POSTGRES_PORT
DB_DATABASE=imday-test
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Setup PostgreSQL container
docker run --name $POSTGRES_CONTAINER_NAME \
  -u $DB_USERNAME \
  -e POSTGRES_PASSWORD=$DB_PASSWORD \
  -e POSTGRES_DB=$DB_DATABASE \
  -p $EXPORTED_POSTGRES_PORT:5432 \
  -d postgres

./mvnw test \
  -DDB_HOST=$DB_HOST \
  -DDB_PORT=$EXPORTED_POSTGRES_PORT \
  -DDB_DATABASE=$DB_DATABASE \
  -DDB_USERNAME=$DB_USERNAME \
  -DDB_PASSWORD=$DB_PASSWORD

```

## Start application in localhost

```shell
#Setup some environment variables
POSTGRES_CONTAINER_NAME=postgres-imday
EXPORTED_POSTGRES_PORT=5433
DB_HOST=localhost
DB_PORT=$EXPORTED_POSTGRES_PORT
DB_DATABASE=imday
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Setup docker
docker run --name $POSTGRES_CONTAINER_NAME \
  -u $DB_USERNAME \
  -e POSTGRES_PASSWORD=$DB_PASSWORD \
  -e POSTGRES_DB=$DB_DATABASE \
  -p $EXPORTED_POSTGRES_PORT:5432 \
  -d postgres

# Start application
./mvnw spring-boot:run \
  -Dexec.mainClass="br.com.uol.imdayapi.ImDayApiApplication" \
  -Dspring-boot.run.jvmArguments="\
      -DDB_HOST=$DB_HOST \
      -DDB_PORT=$EXPORTED_POSTGRES_PORT \
      -DDB_DATABASE=$DB_DATABASE \
      -DDB_USERNAME=$DB_USERNAME \
      -DDB_PASSWORD=$DB_PASSWORD"

# Press Ctrl-Z to suspend the application and use the same terminal

# Insert some users into the database
INSERT_SQL="
TRUNCATE users CASCADE;

ALTER SEQUENCE schedule_id_seq RESTART;
ALTER SEQUENCE users_id_seq RESTART;

INSERT INTO users(name)
VALUES ('Michael'),
       ('Ana'),
       ('Carlos')
RETURNING *;
"

POSTGRES_CONTAINER_ID=$(docker inspect --format="{{.Id}}" $POSTGRES_CONTAINER_NAME)

docker exec -it $POSTGRES_CONTAINER_ID sh \
  -c "psql -U \"$DB_USERNAME\" \
           -d \"$DB_DATABASE\" \
           -c \"$INSERT_SQL\" ";

# Type fg to resume the application and give the terminal back to it

# Open Swagger UI

sensible-browser http://localhost:8080/swagger-ui.html

# Press Ctrl-C to stop the application
```