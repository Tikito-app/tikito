---
sidebar_position: 3
---

# Money

TODO: make tree, start at asset, then split money/security/loan, then split fiat/crypto and stock/etf and lineary/annuitiet/generic


Money can mean that you have a debit account with a bank, a cryptocurrency, or hard cash. 
In the future it will be possible to specify an interest (https://github.com/Tikito-app/tikito/issues/114).

You can specify the starting balance of a debit/credit account. This is useful when you miss any historical transactions. This way, you can still see the proper final balance being calculated.
However, some banks support the final balance field in their exports. If this is the case, Tikito will use that field and ignore and initial offset to the balance of a debit/credit account