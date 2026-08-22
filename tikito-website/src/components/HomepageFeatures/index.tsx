import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Your data, your server',
    description: (
      <>
        Tikito runs entirely on your own hardware using Docker. Your bank balances
        and portfolio data never leave your machine.
      </>
    ),
  },
  {
    title: 'Banks & brokers',
    description: (
      <>
        Import exports from ABN AMRO, ING, Bunq, Bitvavo, and DeGiro. Generic
        CSV/Excel support covers any other bank or broker.
      </>
    ),
  },
  {
    title: 'Portfolio tracking',
    description: (
      <>
        Track stocks, ETFs, and crypto. Historical prices update every night so
        you always see your current performance and dividend income.
      </>
    ),
  },
  {
    title: 'Spending insights',
    description: (
      <>
        Group transactions by description or counterparty to see where your money
        goes. Set budgets and compare them against actual spending.
      </>
    ),
  },
  {
    title: 'Loans & mortgages',
    description: (
      <>
        Model annuity and linear mortgages with multiple loan parts and
        interest-rate periods. Compare scheduled payments with your actual bank
        transactions.
      </>
    ),
  },
  {
    title: 'Net worth over time',
    description: (
      <>
        The overview dashboard combines all accounts into a single net-worth chart
        so you can see the big picture at a glance.
      </>
    ),
  },
];

function Feature({title, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center padding-horiz--md" style={{paddingTop: '1rem', paddingBottom: '1rem'}}>
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
