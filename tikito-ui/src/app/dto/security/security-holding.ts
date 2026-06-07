import {Security} from "./security";
import {SecurityHoldingValue} from "./security-holding-value";

export default class SecurityHolding extends SecurityHoldingValue {
  id: number;
  accountId: number;
  securityId: number;
  security: Security;
}
