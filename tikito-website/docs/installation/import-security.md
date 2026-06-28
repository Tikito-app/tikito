---
sidebar_position: 5
---

# Import security transactions

After creating an account for your broker, you can import your portfolio export.

## Step 1 - Export from your broker

Download a transaction export from your broker. See [supported file formats](/docs/importing/security) for which brokers are supported. For DeGiro, download both the **Account** export and the **Transactions** export.

## Step 2 - Import into Tikito

Click **Import** in the top navigation. Select the broker account you created and upload the file. Tikito will detect the broker format automatically.

If your broker is not supported, use the [generic CSV/Excel format](/docs/importing/security#generic-import-custom-file).

## Step 3 - Enrich securities

After importing, Tikito needs to resolve each security's ISIN to a market symbol (e.g. `ASML` on Euronext) before it can fetch historical prices.

Go to **Admin → Securities** and find the newly imported securities (filter by name if needed). For each one, click the three-dot menu on the right and select **Enrich**. This calls the Finnhub API to find the correct symbol and fetches metadata (sector, industry, exchange).

You only need to do this once per security.

## Step 4 - Fetch historical prices

After enriching, click the three-dot menu again and select **Update prices**. This fetches the full price history for the security.

## Step 5 - Recalculate historical values

Finally, click the three-dot menu and select **Recalculate historical value**. This calculates the value of your holding on each day based on the prices and your transaction history.

Steps 3–5 are triggered automatically in the background as [jobs](/docs/features/jobs) when you import for the first time, but you can also run them manually if something did not process correctly.

## Visualise

Go to **Securities** in the top navigation to see your portfolio overview. Click a position to see details and double-click to open the holding value graph.
