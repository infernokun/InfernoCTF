import { Role } from "./enums/role.enum";
import { StoredObject } from "./stored-object.model";

export class User extends StoredObject {
  username?: string;
  email?: string;
  password?: string;
  role?: Role;
  editMode?: boolean

  constructor(serverResult?: any) {
    if (serverResult) {
      super(serverResult);
      this.username = serverResult.username;
      this.email = serverResult.email;
      this.password = serverResult.password;
      this.role = serverResult.role;
      this.editMode = false;
    }
  }
}
