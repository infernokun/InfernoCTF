import { Role } from "./enums/role.enum";
import { StoredObject } from "./stored-object.model";

export class User extends StoredObject {
  username?: string;
  email?: string;
  password?: string;
  role?: Role;
  editMode = false;

  constructor(serverResult?: any) {
    super(serverResult);

    if (serverResult) {
      this.username = serverResult.username;
      this.role = serverResult.role as Role;
    }
  }
}
