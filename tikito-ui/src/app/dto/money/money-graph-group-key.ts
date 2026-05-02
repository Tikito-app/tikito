
export class MoneyGraphGroupKey {
  name: string;
  isBudget: boolean;
  isHolding: boolean

  constructor(name: string, isBudget: boolean, isHolding: boolean) {
    this.name = name;
    this.isBudget = isBudget;
    this.isHolding = isHolding;
  }

  toString(): string {
    return this.name + '||' + this.isBudget + '||' + this.isHolding;
  }

  static fromString(key: string): MoneyGraphGroupKey {
    let parts = key.split('||');
    return new MoneyGraphGroupKey(parts[0], parts[1] == 'true', parts[2] == 'true');
  }
}