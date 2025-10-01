import {Isin} from "../isin";
import {SecurityType} from "./security-type";

export class Security {
    id: number;
    securityType: SecurityType;
    name: string;
    isins: Isin[];
    currencyId: number;
    sector: string;
    industry: string;
    exchange: string;
    currentIsin: string;
}
