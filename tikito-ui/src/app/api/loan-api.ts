import {HttpService} from "../service/http.service";
import {Observable} from "rxjs";
import {HttpRequestData} from "../dto/http-request-data";
import {Injectable} from "@angular/core";
import {Loan} from "../dto/loan";
import {LoanType} from "../dto/loan-type";
import {LoanInterest} from "../dto/loan-interest";
import {LoanValue} from "../dto/loan-value";

@Injectable({
  providedIn: 'root'
})
export class LoanApi {
  constructor(private http: HttpService) {
  }

  getLoans(): Observable<Loan[]> {
    return this.http.httpGetList<Loan>(Loan, new HttpRequestData().withUrl('/api/loan'));
  }

  createOrUpdateLoan(loanId: number,
                     name: string,
                     groupIds: number[]) {
    return this.http.httpPost<Loan>(new HttpRequestData()
      .withUrl('/api/loan')
      .withBody({
        id: loanId,
        name: name,
        groupIds: groupIds
      }));
  }

  createOrUpdateLoanPart(loanPartId: number,
                         loanId: number,
                         name: string,
                         startDate: string,
                         endDate: string,
                         amount: number,
                         currencyId: number,
                         loanType: LoanType,
                         interests: LoanInterest[]): Observable<Loan> {
    return this.http.httpPost<Loan>(new HttpRequestData()
      .withUrl('/api/loan/part')
      .withBody({
        id: loanPartId,
        loanId: loanId,
        name: name,
        startDate: startDate,
        endDate: endDate,
        amount: amount,
        currencyId: currencyId,
        loanType: loanType,
        interests: interests
      }));
  }

  getLoan(loanId: number): Observable<Loan> {
    return this.http.httpGetSingle<Loan>(Loan, new HttpRequestData().withUrl('/api/loan/' + loanId));
  }

  deleteLoan(loanId: number): Observable<void> {
    return this.http.httpDelete(new HttpRequestData().withUrl('/api/loan/' + loanId));
  }

  deleteLoanPart(loanId: number, loanPartId: number): Observable<void> {
    return this.http.httpDelete(new HttpRequestData().withUrl('/api/loan/' + loanId + '/part/' + loanPartId));
  }

  deleteInterest(loanId: number | null, loanPartId: number | null, interestId: number | undefined) {
    return this.http.httpDelete(new HttpRequestData().withUrl('/api/loan/' + loanId + '/part/' + loanPartId + '/interest/' + interestId));
  }

  getLoanValues(): Observable<LoanValue[]> {
    return this.http.httpGetList<LoanValue>(LoanValue, new HttpRequestData().withUrl('/api/loan/values'));
  }

  getLoanValuesForCurrentDate(): Observable<LoanValue[]> {
    return this.http.httpGetList<LoanValue>(LoanValue, new HttpRequestData().withUrl('/api/loan/values/current'));
  }
}
