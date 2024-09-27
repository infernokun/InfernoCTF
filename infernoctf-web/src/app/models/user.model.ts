import { Role } from "./enums/role.enum";

export class User {
  id?: number;
  username?: string;
  email?: string;
  password?: string;
  role?: Role;
  createdAt?: string;
  editMode = false;

}
