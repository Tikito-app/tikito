import {FileType} from "./file-type";
import {AssetType} from "./asset-type";

export class ImportFileProcessState {
  file: File;
  content: string | ArrayBuffer | null;
  fileType: FileType;
  assetType: AssetType | null;
  parsedContent: string[][];
  csvSeparator: string;
}
