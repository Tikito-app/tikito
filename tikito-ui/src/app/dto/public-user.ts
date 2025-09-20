import {AutocompleteValue} from './autocomplete-value';

export class PublicUser implements AutocompleteValue {
  userId: string;
  displayName: string;

    getDisplayValue(): string {
      return this.displayName;
    }
}
