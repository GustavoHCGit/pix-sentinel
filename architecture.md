# Arquitetura

O Pix Sentinel separa o motor de decisão do adapter HTTP. O domínio recebe uma transação já enriquecida com features e devolve uma decisão determinística, score e motivos.

No MVP atual, a API chama o motor diretamente. Em uma evolução orientada a eventos, o gateway publicaria `pix.transactions.v1` em Kafka/Redpanda. Um consumidor faria o enriquecimento de features, chamaria o motor e publicaria `risk.decisions.v1`. O `transactionId` seria a chave de idempotência.

A decisão é explicável por desenho: cada ponto do score vem de um motivo de regra. `APPROVE` representa baixo risco, `REVIEW` envia para análise manual e `BLOCK` interrompe o fluxo antes da liquidação. No MVP, os limites são parâmetros do `RiskEngine`; a externalização para configuração será feita depois.

Para produção, eu adicionaria Redis para janelas de velocidade, persistência de decisões para auditoria, consumer idempotente, retry com backoff, dead-letter topic, Testcontainers, tracing distribuído e métricas de falso positivo. O modelo de machine learning, caso usado, seria treinado offline e sujeito a avaliação; não substituiria automaticamente a explicabilidade das regras.
