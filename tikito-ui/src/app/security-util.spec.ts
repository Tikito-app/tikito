import {SecurityHoldingValue} from "./dto/security/security-holding-value";
import AggregatedHistoricalHoldingsValue from "./dto/security/aggregated-historical-holdings-value";
import HistoricalHoldingValue from "./dto/security/historical-holding-value";
import {SecurityUtil} from "./security-util";

describe('Utility Functions', () => {

  describe('add', () => {
    it('should add two numbers correctly', () => {
      const result = SecurityUtil.getPerformance(new SecurityHoldingValue());
      expect(result).toBe(5);
    });
  });


  // describe('isEven', () => {
  //   it('should return true for even numbers', () => {
  //     expect(isEven(4)).toBeTrue();
  //     expect(isEven(0)).toBeTrue();
  //     expect(isEven(-2)).toBeTrue();
  //   });
  //
  //   it('should return false for odd numbers', () => {
  //     expect(isEven(3)).toBeFalse();
  //     expect(isEven(1)).toBeFalse();
  //     expect(isEven(-3)).toBeFalse();
  //   });
  // });
});
