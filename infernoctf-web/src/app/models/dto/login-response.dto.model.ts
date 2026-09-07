import { User } from "../user.model";

export class LoginResponseDTO {
  jwt?: string;
  // No refreshToken: the grant is an httpOnly cookie and never reaches JavaScript.
  user?: User;

  constructor(serverResult?: any) {
    if (serverResult) {
      this.jwt = serverResult.jwt;
      this.user = serverResult.user ? new User(serverResult.user) : undefined;
    }
  }
}
