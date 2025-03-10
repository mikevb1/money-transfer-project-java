# Temporal meetup project: Money transfer

Dit project is een aanvulling op deze tutorial van Temporal:
[Run your first app tutorial](https://docs.temporal.io/docs/java/run-your-first-app-tutorial)


## Lokaal starten 

- Run de docker-compose file en start de container
- Start de back-end
- Run de front-end (prerequisites: Node.js en npm)
  - cd react-ui
  - npm run dev

## Vervolg

Mogelijk opties om aan dit project toe te voegen zijn:
- Logging
- Een notificatie service (bijv. email/push)
- Versleutelde data opslag
- Mogelijkheid tot annuleren voordat een transfer geapproved wordt
- Batch transfers
- Categorisatie van transfers
- Transactie handmatig approve nodig uitbreiden obv:
  - IBAN

## Samenvatting applicatie

Deze applicatie is een Java Spring Boot-service voor geldtransacties met workflow-ondersteuning via Temporal. Het systeem automatiseert geldtransacties en biedt handmatige goedkeuring voor transacties boven de €1000.

### Temporal componenten
- Workflows: Gedefinieerd in MoneyTransferWorkflowImpl, deze orkestreert de transactie.

- Activities: Afzonderlijke bewerkingen zoals withdraw, deposit en refund worden als activiteiten uitgevoerd.

- WorkflowClient: Gebruikt voor interactie met Temporal en het starten van workflows.

- Workers: Verantwoordelijk voor het uitvoeren van workflows en activiteiten.

### Workflow stappen

1. Start de workflow: Een nieuwe transactie wordt gecreëerd en de workflow wordt opgestart via het POST /transaction/start endpoint.

2. Uitvoeren van activiteiten:

- Withdraw: Geld wordt afgeschreven van de bronrekening.

- Goedkeuringsstap (indien vereist): Indien het bedrag groter is dan €1000, pauzeert de workflow en wacht op goedkeuring via POST /transaction/approve/{reference}.

- Deposit: Bij goedkeuring wordt het geld bijgeschreven op de doelrekening.

- Refund (bij mislukking of afkeuring): Bij afkeuring of een fout wordt het geld teruggestort naar de oorspronkelijke rekening.

3. Workflow voltooid: De status van de transactie wordt bijgewerkt en opgeslagen in de database.