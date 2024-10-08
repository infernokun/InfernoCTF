import { SimpleFormData, TextQuestion } from "./SimpleFormData.model";

export class Room {
  id: string = '';
  name: string = '';
  createdAt: Date = new Date();
  creator: string = '';
  facilitators: string[] = [];
  surroundTag: string = '';
}

export class RoomFormData extends SimpleFormData {
  constructor(
    updateResultsCB: Function = (k: any, v: any) => { }
  ) {
    super('room');

    this.questions.push(
      new TextQuestion({
        label: 'Name',
        key: 'name',
      }),
      new TextQuestion({
        label: 'SurroundTag',
        key: 'surroundTag',
      }),
    );

    this.questions.forEach((question) => (question.cb = updateResultsCB));
  }
}
