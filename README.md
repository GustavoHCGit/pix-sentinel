# Pix Sentinel

> Motor de decisão de risco em tempo real para transações Pix, desenvolvido como projeto educacional de engenharia backend e dados.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F?logo=springboot&logoColor=white)
![Tests](https://img.shields.io/badge/tests-passing-2ea44f)

## Problema

Fraude precisa ser analisada enquanto a transação acontece. O sistema recebe um evento Pix, calcula sinais de risco, aplica regras explicáveis e retorna uma decisão: `APPROVE`, `REVIEW` ou `BLOCK`.

O projeto não tenta reproduzir sistemas internos de nenhum banco e não usa dados reais. O MVP atual é uma API síncrona com regras auditáveis. Kafka/Redpanda aparecem como próxima etapa, para não vender como implementado aquilo que ainda é roadmap.

## Fluxo

```text
Pix request -> Risk API -> Rule engine -> Decision
                                      |
                                      v
                              reasons + risk score
```

A primeira versão implementa cinco sinais: valor acima do limite, conta recém-criada, dispositivo nunca visto, muitas transações em uma janela curta e distância atípica entre localizações.

As regras produzem motivos estruturados. Portanto, a decisão é explicável e pode ser auditada.

## Exemplo

```bash
curl -X POST http://localhost:8080/v1/risk/evaluate \
  -H 'Content-Type: application/json' \
  -d '{
    "transactionId": "pix-001",
    "accountId": "account-123",
    "amount": 4200.00,
    "currency": "BRL",
    "accountAgeDays": 12,
    "devicePreviouslySeen": false,
    "transactionsLast10Minutes": 5,
    "distanceFromUsualLocationKm": 180
  }'
```

Resposta esperada:

```json
{
  "transactionId": "pix-001",
  "decision": "BLOCK",
  "riskScore": 100,
    "reasons": ["AMOUNT_ABOVE_LIMIT", "NEW_ACCOUNT", "NEW_DEVICE", "HIGH_TRANSACTION_VELOCITY", "UNUSUAL_LOCATION"]
}
```

## O que este projeto demonstra

**Backend:** Kotlin, Spring Boot, API REST, validação, domínio isolado e testes automatizados.

**Preparação para sistemas distribuídos:** contrato do fluxo, proposta de tópicos versionados, idempotência por `transactionId` e estratégia de retry documentada para a próxima etapa.

**Dados e risco:** features de comportamento, regras determinísticas, score, revisão manual e trilha de auditoria.

**Engenharia:** separação de domínio e adapters, observabilidade, configuração externa e decisões registradas.

## Execução

Pré-requisito: Java 21.

```bash
./gradlew test
./gradlew bootRun
```

A infraestrutura opcional está em `infra/docker-compose.yml` e sobe PostgreSQL e Redpanda para as próximas etapas. O MVP atual não depende deles para executar os testes.

## Evolução planejada

1. Adicionar Redis para features de baixa latência.
2. Consumir eventos Pix via Kafka/Redpanda.
3. Criar uma janela de velocidade com estado por conta.
4. Adicionar Testcontainers e testes de contrato.
5. Instrumentar latência p95 e taxa de falso positivo com Micrometer.
6. Criar um pipeline de treinamento offline usando dados sintéticos, sem automatizar bloqueios diretamente por modelo.

## Limites do projeto

Este é um projeto de estudo. Não implementa autenticação, liquidação Pix, LGPD, integração com SPI, biometria ou modelo de machine learning em produção.

## Licença

MIT.
