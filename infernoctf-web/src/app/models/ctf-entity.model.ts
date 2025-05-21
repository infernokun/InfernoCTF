import { Flag } from "./flag.model";
import { StoredObject } from "./stored-object.model";

export class CTFEntity extends StoredObject {
    question?: string;
    maxAttempts?: number;
    description?: string;
    hints?: string[];
    category?: string;
    difficultyLevel?: string;
    points?: number;
    author?: string;
    flags?: Flag[];
    tags?: string[];
    visible?: boolean;
    releaseDate?: Date;
    expirationDate?: Date;
    attachments?: string[];
    solutionExplanation?: string;
    relatedChallenges?: string[];

    constructor(serverResult?: any) {
        super(serverResult);
        if (serverResult) {
            this.question = serverResult.question;
            this.maxAttempts = serverResult.maxAttempts;
            this.description = serverResult.description;
            this.hints = serverResult.hints;
            this.category = serverResult.category;
            this.difficultyLevel = serverResult.difficultyLevel;
            this.points = serverResult.points;
            this.author = serverResult.author;
            this.flags = serverResult.flags;
            this.tags = serverResult.tags;
            this.visible = serverResult.visible;
            this.releaseDate = serverResult.releaseDate;
            this.expirationDate =serverResult.expirationDate;
            this.attachments = serverResult.solutionExplanation;
            this.relatedChallenges = serverResult.relatedChallenges;
        }
    }
}