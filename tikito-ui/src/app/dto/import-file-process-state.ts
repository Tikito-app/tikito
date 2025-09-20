import {FileType} from "./file-type";
import {AccountType} from "./account-type";

export class ImportFileProcessState {
  file: File;
  content: string | ArrayBuffer | null;
  fileType: FileType;
  accountType: AccountType | null;
  parsedContent: string[][];
  csvSeparator: string;
}
