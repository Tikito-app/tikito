import type {ReactNode} from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <Heading as="h1" className="hero__title">
                    {siteConfig.title}
                </Heading>
                <p className="hero__subtitle">{siteConfig.tagline}</p>
                <br/>
                {/*<div className="mt-8">*/}
                {/*    <p className="max-w-1/4 m-auto mt-4 px-4 text-lg text-gray-700 dark:text-gray-100">*/}
                {/*        Whether you want to know how your stock portfolio is doing, or what to expect on your debit*/}
                {/*        account, Tikito helps you with this, without sacrificing on your privacy*/}
                {/*    </p>*/}
                {/*</div>*/}

                <div className={styles.buttons}>
                    <Link
                        className="button button--secondary button--lg"
                        to="/docs/overview/quick-start">
                        Start now!
                    </Link>
                </div>
            </div>

            {/*<img src="/img/logo.png" alt="" />*/}

        </header>


    );
}

export default function Home(): ReactNode {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout
            title={`${siteConfig.title}`}
            description="Description will go into a meta tag in <head />">
            <HomepageHeader/>
            <main>
                <div className="container">
                    <br/>
                    <h2>Stock overview</h2>
                    <img src="/tikito-screenshots/tikito_security_graph.png" alt="" width="70%"/>
                    <br/>
                    <h2>Debit account overivew</h2>
                    <img src="/tikito-screenshots/tikito_money_graph.png" alt="" width="70%"/>
                    <br/>
                    <br/>
                    <h2>Complete portfolio overivew</h2>
                    <img src="/tikito-screenshots/tikito_stock_overiew.png" alt="" width="70%" />
                    <br/>
                    <h2>Money import</h2>
                    <img src="/tikito-screenshots/tikito_money_import.png" alt="" width="70%"/>
                    <br/>
                    <h2>Configuring import headers</h2>
                    <img src="/tikito-screenshots/tikito_custom_header_import.png" alt="" width="70%"/>
                </div>
            </main>
        </Layout>
    );
}
