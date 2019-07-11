/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.battlesnake;

import com.battlesnake.data.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class RequestController {

    @RequestMapping(value = "/start", method = RequestMethod.POST, produces = "application/json")
    public StartResponse start(@RequestBody StartRequest request) {
        return new StartResponse()
                .setName("Simple Snake")
                .setColor("#FF3497")
                .setHeadUrl("http://vignette1.wikia.nocookie.net/nintendo/images/6/61/Bowser_Icon.png/revision/latest?cb=20120820000805&path-prefix=en")
                .setHeadType(HeadType.DEAD)
                .setTailType(TailType.PIXEL)
                .setTaunt("I can find food!");
    }

    @RequestMapping(value = "/move", method = RequestMethod.POST, produces = "application/json")
    public MoveResponse move(@RequestBody MoveRequest request) {
        MoveResponse moveResponse = new MoveResponse();

        Snake mySnake = findOurSnake(request); // kind of handy to have our snake at this level

        List<Move> towardsFoodMoves = moveTowardsFood(request, mySnake.getCoords()[0]);

        if (towardsFoodMoves != null && !towardsFoodMoves.isEmpty()) {
            Move move = getValidMove(towardsFoodMoves, mySnake);
            if (move == null) {
                // les go over the other valid options
                List<Move> newMoves = getNewMoves(towardsFoodMoves);
                move = getValidMove(newMoves, mySnake);
            }
            return moveResponse.setMove(move).setTaunt("I'm hungry");
        } else {
            return moveResponse.setMove(Move.DOWN).setTaunt("Oh Drat");
        }
    }

    boolean isOutOfBounds(MoveRequest request, Snake snake) {
        int width = request.getWidth();
        int height = request.getHeight();

        return false;
    }

    List<Move> getNewMoves(List<Move> towardsFoodMoves) {
        Set<Move> allMoves = new HashSet<>(Arrays.asList(Move.values()));

        List<Move> newMoves = new ArrayList<>();
        for (Move move : allMoves) {
            if (!towardsFoodMoves.contains(move)) {
                newMoves.add(move);
            }
        }
        return newMoves;
    }

    private Move getValidMove(List<Move> moves, Snake snake) {
        int[] head = snake.getCoords()[0];
        for (Move move : moves) {
            switch (move) {
            case UP:
                int[] up = head.clone();
                up[1] = up[1] - 1;

                int[] up2 = head.clone();
                up2[1] = up2[1] - 2;

                if (!collideWithSnake(snake, up) && !collideWithSnake(snake, up2)) {
                    return Move.UP;
                }
                break;
            case DOWN:
                int[] down = head.clone();
                down[1] = down[1] + 1;

                int[] down2 = head.clone();
                down2[1] = down2[1] + 2;

                if (!collideWithSnake(snake, down) && !collideWithSnake(snake, down2)) {
                    return Move.DOWN;
                }
                break;
            case LEFT:
                int[] left = head.clone();
                left[0] = left[0] - 1;

                int[] left2 = head.clone();
                left2[0] = left2[0] - 1;

                if (!collideWithSnake(snake, left) && !collideWithSnake(snake, left2)) {
                    return Move.LEFT;
                }
                break;
            case RIGHT:
                int[] right = head.clone();
                right[0] = right[0] + 1;

                int[] right2 = head.clone();
                right2[0] = right2[0] + 1;

                if (!collideWithSnake(snake, right) && !collideWithSnake(snake, right2)) {
                    return Move.RIGHT;
                }
            }
        }
        return null;
    }

    @RequestMapping(value = "/end", method = RequestMethod.POST)
    public Object end() {
        // No response required
        Map<String, Object> responseObject = new HashMap<String, Object>();
        return responseObject;
    }

    /*
     *  Go through the snakes and find your team's snake
     *  
     *  @param  request The MoveRequest from the server
     *  @return         Your team's snake
     */
    private Snake findOurSnake(MoveRequest request) {
        String myUuid = request.getYou();
        List<Snake> snakes = request.getSnakes();
        return snakes.stream().filter(thisSnake -> thisSnake.getId().equals(myUuid)).findFirst().orElse(null);
    }

    /*
     *  Simple algorithm to find food
     *  
     *  @param  request The MoveRequest from the server
     *  @param  request An integer array with the X,Y coordinates of your snake's head
     *  @return         A Move that gets you closer to food
     */
    public ArrayList<Move> moveTowardsFood(MoveRequest request, int[] mySnakeHead) {
        ArrayList<Move> towardsFoodMoves = new ArrayList<>();

        int[] firstFoodLocation = request.getFood()[0];

        if (firstFoodLocation[0] < mySnakeHead[0]) {
            towardsFoodMoves.add(Move.LEFT);
        }

        if (firstFoodLocation[0] > mySnakeHead[0]) {
            towardsFoodMoves.add(Move.RIGHT);
        }

        if (firstFoodLocation[1] < mySnakeHead[1]) {
            towardsFoodMoves.add(Move.UP);
        }

        if (firstFoodLocation[1] > mySnakeHead[1]) {
            towardsFoodMoves.add(Move.DOWN);
        }

        return towardsFoodMoves;
    }

    public boolean collideWithSnake(Snake snake, int[] coords) {

        for (int[] body : snake.getCoords()) {
            if (body[0] == coords[0] && body[1] == coords[1]) {
                return true;
            }
        }
        return false;
    }

}
