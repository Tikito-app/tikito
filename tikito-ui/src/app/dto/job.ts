import {JobType} from "../job-type";

export class Job {

  timestamp: string;
  jobType: JobType;
  securityId: number;
  accountId: number;
  loanId: number;
}