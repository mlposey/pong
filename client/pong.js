// Entity represents a visible object with canvas coordinates.
class Entity {
    constructor(xPos, yPos, width, height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
    }

    draw(ctx) {
        ctx.fillRect(this.xPos, this.yPos, this.width, this.height);
    }
}

// ScoreBoard stores and paints the scores of each player.
class ScoreBoard {
    constructor() {
        this.scores = [0, 0];
        // width/height of a character in pixels
        this.charPixelSize = 10;
        // A cached canvas for each number in [0, 9]      
        this.numbers = [
            '111101101101111', // 0
            '010010010010010', // 1
            '111001111100111', // 2
            '111001111001111', // 3
            '101101111001001', // 4
            '111100111001111', // 5
            '111100111101111', // 6
            '111001001001001', // 7
            '111101111101111', // 8
            '111101111001111'  // 9
        ].map(number => {
            // A canvas that will only hold a number.
            const canv = document.createElement('canvas');
            const s = this.charPixelSize;
            canv.height = s * 5;
            canv.width  = s * 3;
            const context = canv.getContext('2d');
            context.fillStyle = '#FFF';
            number.split('').forEach((fill, i) => {
                if (fill === '1') {
                    context.fillRect((i % 3) * s, (i / 3 | 0) * s, s, s);
                }
            });
            return canv;
        });
    }

    // Draws the score of each player onto the canvas
    draw(canvas, ctx) {
        // Split the main canvas into three horizontal segments.
        const segmentWidth = canvas.width / 3;
        const charWidth = this.charPixelSize * 4;
        // Use the cached numbers to paint the score of each player onto
        // the canvas.
        this.scores.forEach((score, i) => {
            const chars = score.toString().split('');
            const offset = segmentWidth * (i + 1) -
                           (charWidth * chars.length / 2) +
                           this.charPixelSize / 2;
            chars.forEach((char, j) => {
                ctx.drawImage(this.numbers[char|0],
                                  offset + j * charWidth, 20);
            });
        });
    }
}

// Game stores and paints the state of the players, score, and ball.
// This class does not calculate game logic. That task is left
// to the server.
class Game {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.scoreboard = new ScoreBoard();
        this.playerId = 0;

        this.createEntities();
        this.drawLobbyView();
    }

    // Instantiates the players and ball
    createEntities() {
        const kWidth = 20;
        this.players = [
            new Entity(kWidth, (this.canvas.height - 100) / 2, kWidth, 100),
            new Entity(this.canvas.width - 2 * kWidth,
                       (this.canvas.height - 100) / 2, kWidth, 100)
        ];
        this.ball = new Entity((this.canvas.width - kWidth) / 2,
                               (this.canvas.height - kWidth) / 2, kWidth, kWidth);
    }

    // Draws on the canvas temporary entities and a lobby message
    drawLobbyView() {
        this.drawState();

        this.ctx.font = '20px Arial';
        this.ctx.fillStyle = '#FFF';
        this.ctx.textAlign = 'center';
        this.ctx.fillText('Waiting on another player...', this.canvas.width / 2,
                          this.canvas.height / 2 - 60);
        this.ctx.fillStyle = '#000';
    }

    // Draws all entities onto the canvas
    drawState() { 
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        this.ctx.save();
        this.ctx.fillStyle = "#FFF";

        this.players[0].draw(this.ctx);
        this.players[1].draw(this.ctx);
        this.ball.draw(this.ctx);
        this.scoreboard.draw(this.canvas, this.ctx);    

        this.ctx.restore();
    }
}

// A WebSocket connection to the game server
var socket = new WebSocket('ws://srv.marcusposey.com:8001');
socket.binaryType = 'arraybuffer';
// A directive is the first 8 bits of a message to the game server. This
// is used to describe the intent of all communication.
const directives = Object.freeze({
    id: 0,
    gameState: 1,
    movePlayerUp: 2,
    movePlayerDown: 3,
    stopMovingPlayer: 4,
    gameScore: 5,
});

// Handle messages sent from the game server.
socket.onmessage = function(event) {
    let msg = new DataView(event.data);
    let directive = msg.getInt8(0);
    switch (directive) {
        // Get the player's id when they connect to the server.
        case directives.id:
            game.playerId = msg.getInt32(1);
            break;

        // Get the state of the game.
        case directives.gameState:
            game.players[0].xPos = msg.getInt32(1);
            game.players[0].yPos = msg.getInt32(5);
            game.players[1].xPos = msg.getInt32(9);
            game.players[1].yPos = msg.getInt32(13);
            game.ball.xPos = msg.getInt32(17);
            game.ball.yPos = msg.getInt32(21);
            game.drawState();
            break;

        // Get the scores of each player.
        case directives.gameScore:
            game.scoreboard.scores[0] = msg.getInt32(1);
            game.scoreboard.scores[1] = msg.getInt32(5);
            game.drawState();
            break;
    }
};

// Signals to the server that the client player would like to move in a
// new direction or stop moving
function updateMovement(directive) {
    let message = new ArrayBuffer(5);
    let dv = new DataView(message);
    dv.setInt8(0, directive);
    dv.setInt32(1, game.playerId);
    socket.send(message);
}

// Registers keyboard and touch events which dictate player movement
function registerEventHandlers() {
    const kUpKey = 38;
    const kDownKey = 40;

    document.addEventListener('keydown', function(event) {
        if (event.keyCode == kUpKey) updateMovement(directives.movePlayerUp);
        else if (event.keyCode == kDownKey) updateMovement(directives.movePlayerDown);
    });
    document.addEventListener('keyup', function(event) {
        updateMovement(directives.stopMovingPlayer);
    });

    document.addEventListener('touchstart', function(event) {
        let y = event.touches[0].clientY;
        if (y > window.innerHeight / 2) updateMovement(directives.movePlayerDown);
        else if (y < window.innerHeight / 2) updateMovement(directives.movePlayerUp);
    });
    document.addEventListener('touchend', function(event) {
        updateMovement(directives.stopMovingPlayer);
    });
}

// The game state
var game;

function main() {
    game = new Game(document.getElementById('game'));

    registerEventHandlers();
}
