---
sidebar_position: 1
---

# Jobs

Tikito uses background jobs to update your data. 
Every night it will update the prices for securities.
When you import securities, the holding values of all affected securities will be updated, and so will the aggregated holding values.
The same applies to money imports.

When the import of a security leads to a new holding, there will also be a job to enrich the security.