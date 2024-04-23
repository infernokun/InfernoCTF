import { Flag } from "./flag.model";

export class CTFEntity {
    id: string | undefined;
    question: string | undefined;
    maxAttempts: number | undefined;
    description: string | undefined;
    hints: string[] | undefined;
    category: string | undefined;
    difficultyLevel: string | undefined;
    points: number | undefined;
    author: string | undefined;
    flags: Flag[] | undefined;
    tags: string[] | undefined;
    visible: boolean | undefined;
    releaseDate: Date | undefined;
    expirationDate: Date | undefined;
    attachments: string[] | undefined;
    solutionExplanation: string | undefined;
    relatedChallenges: string[] | undefined;

    /*constructor(serverResult?: any) {
        if (serverResult) {
            this.id = serverResult.id || undefined;

            this.appHealthCheckResult = serverResult.appHealthCheckResult || '';
            this.classification = serverResult.classification || undefined;

            this.latencyMilliSecs = serverResult.latencyMilliSecs || undefined;

            this.name = serverResult.name || undefined;
            this.suiteName = serverResult.suiteName || undefined;

            this.sourceSystem = serverResult.sourceSystem || undefined;

            this.epochTime = serverResult.epochTime ? moment(serverResult.epochTime).format("YYYY-MM-DD HH:mm:ss.SSS") : undefined;
            this.timestamp = serverResult.timestamp ? moment(serverResult.timestamp).format("YYYY-MM-DD HH:mm:ss.SSS") : undefined;

            HealthCheck.position++;
        }
    }*/
}