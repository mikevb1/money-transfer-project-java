# Temporal meetup project: Money transfer

In dit project is een aanvulling op deze tutorial van Temporal:
[Run your first app tutorial](https://docs.temporal.io/docs/java/run-your-first-app-tutorial)


## Stappen

- Run de docker-compose file en start de container
- Start de back-end
- Run de front-end
  - cd react-ui
  - npm run dev

## Vervolg

Mogelijk opties om aan dit project toe te voegen zijn:
- Logging
- Email service
- 

## Building, cleaning, and other tasks 

```
build:
    mvn clean install -Dorg.slf4j.simpleLogger.defaultLogLevel=info 2>/dev/null

clean:
    mvn clean -q -Dmaven.logging.level=0

worker:
    mvn compile exec:java -Dexec.mainClass="moneytransferapp.temporal.MoneyTransferWorker" -Dorg.slf4j.simpleLogger.defaultLogLevel=warn

run:
    mvn compile exec:java -Dexec.mainClass="moneytransferapp.temporal.TransferApp" -Dorg.slf4j.simpleLogger.defaultLogLevel=warn

serve:
    `command -v temporal` server start-dev --log-level=never &

stop-server:
    pkill temporal
```
