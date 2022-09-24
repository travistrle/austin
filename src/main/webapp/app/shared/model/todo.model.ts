import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';

export interface ITodo {
  id?: number;
  task?: string;
  scheduledTime?: string;
  validUntil?: string | null;
  createdDate?: string | null;
  lastModifiedDate?: string | null;
  createdBy?: number | null;
  lastModifiedBy?: number | null;
  users?: IUser[];
}

export const defaultValue: Readonly<ITodo> = {};
