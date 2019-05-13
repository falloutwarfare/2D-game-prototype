import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. By default GameCore
// will handle the 'Escape' key to quit the game but you should
// override this with your own event handler.

/**
 * @author Darren Morgan
 *
 */
@SuppressWarnings("serial")

public class Game extends GameCore {
	// Useful game constants
	static int screenWidth = 1900;
	static int screenHeight = 900;

	float lift = 0.005f;
	float gravity = 0.0001f;

	// Game state flags
	boolean moveJump = false;
	boolean moveRight = false;
	boolean moveLeft = false;
	boolean moveDown = false;
	
	boolean playerAttacking = false;

	int xo;
	int yo;
	int xoffset;
	int yoffset;
	
	int direction = 1;
	
	// Game resources
	Animation idleRight;
	Animation movingRight;
	Animation flyRight;
	
	Animation idleLeft;
	Animation movingLeft;
	Animation flyLeft;
	
	Animation attackRight;
	Animation attackLeft;
	
	long beforeTime, timeDiff, beforeAttack, afterAttack ;

	Image background;

	Sprite player = null;
	ArrayList<Sprite> backgroundShips = new ArrayList<Sprite>();
	ArrayList<Sprite> enemies = new ArrayList<Sprite>();
	Sprite planet = null;

	String loadedMap = "map1";
	
	TileMap tmap = new TileMap(); // Our tile map, note that we load it in init()
	TileMap tmap2 = new TileMap();				
	
	int xpTop, ypTop;
	int xpBottom, ypBottom;
	int xpLeft, ypLeft;
	int xpRight, ypRight;

	Sound theme;


	long total; // The score will be the total time elapsed since a crash
	long score = 0;


	Graphics2D g;

	/**
	 * The obligatory main method that creates an instance of our class and
	 * starts it running
	 * 
	 * @param args
	 *            The list of parameters this program might use (ignored)
	 */
	public static void main(String[] args) {

		Game gct = new Game();
		gct.init();
		// Start in windowed mode with the given screen height and width
		gct.run(false, screenWidth, screenHeight);
	}

	/**
	 * Initialise the class, e.g. set up variables, load images, create
	 * animations, register event handlers
	 */
	public void init() {
		Sprite s; // Temporary reference to a sprite
		Sprite en;

		xoffset = 175;
		yoffset = screenHeight - 600;
		
		try {
			background = ImageIO.read(new File("images/background.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}


		
		
		// Load the tile map and print it out so we can check it is valid
		tmap.loadMap("maps", "map.txt");

		// Create a set of background sprites that we can
		// rearrange to give the illusion of motion

		idleRight = new Animation();
		idleRight.loadAnimationFromSheet("images/PC-idleRight.png", 4, 1, 300);
		
		movingRight = new Animation();
		movingRight.addFrame(loadImage("images/PC-moveRight.png"),1000);
		
		flyRight = new Animation();
		flyRight.addFrame(loadImage("images/PC-flyRight.png"),1000);
		
		idleLeft = new Animation();
		idleLeft.loadAnimationFromSheet("images/PC-idleLeft.png", 4, 1, 300);

		movingLeft = new Animation();
		movingLeft.addFrame(loadImage("images/PC-moveLeft.png"),1000);
		
		flyLeft = new Animation();
		flyLeft.addFrame(loadImage("images/PC-flyLeft.png"),1000);
		
		attackRight = new Animation();
		attackRight.loadAnimationFromSheet("images/attackingRight.png", 5, 1, 60);
		
		attackLeft = new Animation();
		attackLeft.loadAnimationFromSheet("images/attackingLeft.png", 5, 1, 60);
		
		// Initialise the player with an animation
		player = new Sprite(idleRight);


		// Load a single cloud animation
		Animation ca = new Animation();
		ca.addFrame(loadImage("images/bgShips.png"), 1000);
		
		Animation p = new Animation();
		p.addFrame(loadImage("images/iceGiant.png"), 1000);

		Animation ea = new Animation();
		ea.loadAnimationFromSheet("images/enemyWalkRight.png", 6, 1, 60);
		
		planet = new Sprite(p);
		planet.setX(800);
		planet.setY(400);
		planet.show();
		
		// Create 3 clouds at random positions off the screen
		// to the right
		for (int c = 0; c < 3; c++) {
			s = new Sprite(ca);
			s.setX(c * 400);
			s.setY(30 + (c*4) + (int) (Math.random() * 150.0f));
			s.setVelocityX(-0.02f);
			s.show();
			backgroundShips.add(s);
		}

		for (int i = 0; i < 3; i++) {
			en = new Sprite(ea);
			en.setX(80 + (i*200));
			en.setY(40);
			en.setVelocityX(0.05f);
			en.show();
			enemies.add(en);
		}
		
		initialiseGame();

		System.out.println(tmap);
	}

	/**
	 * You will probably want to put code to restart a game in a separate method
	 * so that you can call it to restart the game.
	 */
	public void initialiseGame() {
		total = 0;

		player.setX(64);
		player.setY(48);
		player.setVelocityX(0);
		player.setVelocityY(0);
		player.show();
		
		theme = new Sound("sounds/RulesofNature.wav");
		theme.start();

	}

	/**
	 * Draw the current state of the game
	 */
	public void draw(Graphics2D g) {

		this.g = g;

		// Be careful about the order in which you draw objects - you
		// should draw the background first, then work your way 'forward'

		// First work out how much we need to shift the view
		// in order to see where the player is.
		xo = 0;
		yo = 0;

		// If relative, adjust the offset so that
		// it is relative to the player

		// ...?



		g.drawImage(background, 0, 0, null);

		int xo = (int)-player.getX()+xoffset;
		int yo = (int)-player.getY()+yoffset;


		// Apply offsets to sprites then draw them
		for (Sprite s : backgroundShips) {
			s.setOffsets(xo, yo);
			s.draw(g);
		}
		
		for (Sprite en : enemies) {
			en.setOffsets(xo, yo);
			en.draw(g);
		}
		
		planet.setOffsets(xo, yo);
		planet.draw(g);

		// Apply offsets to player and draw
		player.setOffsets(xo, yo);
		player.draw(g);



		// Apply offsets to tile map and draw it
		tmap.draw(g, xo, yo);


		
		// Show score and status information
		String msg = String.format("Score: %d", total);
		g.setColor(Color.ORANGE);
		g.setFont(new Font("OCR A Std", Font.PLAIN, 18));
		g.drawString(msg, getWidth() - 180, 50);

	}

	public void handleDecel()
	{
		
		if (player.getVelocityX() != 0 && !moveJump && !moveRight && !moveLeft && !moveDown) 
		{
			if (player.getVelocityX() > 0.03) 
			{
				if(!moveRight)
				{
				player.setAnimation(movingRight);
				}
				player.setVelocityX(player.getVelocityX() - 0.0025f);

			} 
			
			else if (player.getVelocityX() < -0.03) 
			{

				if(!moveLeft)
				{
					player.setAnimation(movingLeft);
				}
				player.setVelocityX(player.getVelocityX() + 0.0025f);

				
			}
			
			else if (player.getVelocityX() > -0.03 && player.getVelocityX() < 0) 
			{
				
				if(!moveLeft)
				{
				player.setAnimation(idleLeft);
				}
				player.setVelocityX(0);

				
			}
			
			else if (player.getVelocityX() < 0.03 && player.getVelocityX() > 0) 
			{

				if(!moveRight)
				{
				player.setAnimation(idleRight);
				}
				
				player.setVelocityX(0);
				

			}

		}
		
		if(player.getVelocityX() == 0 && !playerAttacking && direction == 1)
		{
			
			player.setAnimation(idleRight);
			
		}
		
		if(player.getVelocityX() == 0 && !playerAttacking && direction == -1)
		{
			
			player.setAnimation(idleLeft);
			
		}
		
	}
	
	public void handlePCMovement()
	{
		
		
		if (moveJump) 
		{

			if (player.getJumps() > 0 && player.getJumpReady() == true) 
			{

				player.toggleJumpReady();
				player.setAnimationSpeed(1.8f);
				player.setVelocityY(-0.20f);

			}

		} 
		
		if (moveRight) {

			direction = 1;

			player.setAnimation(movingRight);
	
			timeDiff = System.currentTimeMillis() - beforeTime;
				
			if(timeDiff >= 2000)
			{
					
				player.setAnimation(flyRight);
				
				player.setAnimationSpeed(1.8f);
				
				player.setVelocityX(0.3f);
				
			}
			else if(timeDiff < 2000)
			{

				player.setAnimationSpeed(1.8f);
				player.setVelocityX(0.15f);
			
			}
		} 
		else if (moveLeft) {

			direction = -1;
			
			player.setAnimation(movingLeft);
			
			timeDiff = System.currentTimeMillis() - beforeTime;
			
			if(timeDiff >= 2000)
			{
					
				player.setAnimation(flyLeft);
				
				player.setAnimationSpeed(1.8f);
				player.setVelocityX(-0.3f);
				
			}
			else if(timeDiff < 2000)
			{

				player.setAnimationSpeed(1.8f);
				player.setVelocityX(-0.15f);
			
			}

		} 
		
		if (moveDown) {

			player.setAnimationSpeed(1.8f);
			player.setVelocityY(0.15f);

		}
		

		handleDecel();
		
	}
	
	public void handleAttacking(Sprite player){
		
		if(playerAttacking)
		{
			
			afterAttack = System.currentTimeMillis() - beforeAttack;

			
			if(direction == 1)
			{
				
				player.setAnimation(attackRight);

			}
			
			else if(direction == -1)
			{
				
				player.setAnimation(attackLeft);

			}
			
			handleAttackCollision();
			
			if(afterAttack > 300)
			{
				
				playerAttacking = false;
				
				if(!moveJump && !moveRight && !moveLeft && !moveDown)
				{
					
					if(direction == 1)
					{
						
						player.setAnimation(idleRight);
						
					}
					
					else if(direction == -1)
					{
						
						player.setAnimation(idleLeft);
						
					}
					

					
				}
				
			}
			
		}
		
		
	}
	
	public void handleAttackCollision()
	{
		
	
		int playerXRight = (int)(player.getX() + player.getWidth()+ 20);
		int playerXLeft = (int)(player.getX() - 20);


		for (int i = 0; i < enemies.size(); i++)
		{
				
			Sprite en = enemies.get(i);
			
			if(playerXRight > en.getX() && playerXRight < (en.getX()) + en.getWidth() && (player.getY()  > en.getY()) && (player.getY() < (en.getY() + en.getHeight())))
			{
					

				if(en.getActive() == true)
				{
					
					if(direction == 1)
					{
						en.hide();
						score += 100;
						en.setActive();
					}
				}
					
			}

			else if(playerXLeft < (en.getX() + en.getWidth()) 	&& 	playerXLeft > en.getX() && (player.getY() > en.getY() && player.getY() < (en.getY() + en.getHeight())))
			{
					
				if(en.getActive() == true)
				{
					if(direction == -1)
					{
						en.hide();
						score += 100;
						en.setActive();
					}
				}
				
					
			}	
		}
	}

	public void handlePlayerEnemyCollision(Sprite en)
	{
		
		int playerXRight = (int)(player.getX() + player.getWidth());
		int playerXLeft = (int)(player.getX());
		
		if((playerXRight - 20) > en.getX() && (playerXRight - 20) < (en.getX()) + en.getWidth() && (player.getY()  > en.getY()) && (player.getY() < (en.getY() + en.getHeight())))
		{
				

			if(en.getActive() == true)
			{
				
				player.setX(64);
				player.setY(48);
				player.setVelocityX(0);
				player.setVelocityY(0);
				
				
			}
				
		}

		else if((playerXLeft + 10) < (en.getX() + en.getWidth()) 	&& 	(playerXLeft + 10) > en.getX() && (player.getY() > en.getY() && player.getY() < (en.getY() + en.getHeight())))
		{
				
			if(en.getActive() == true)
			{
				
				player.setX(64);
				player.setY(48);
				player.setVelocityX(0);
				player.setVelocityY(0);
				
			}
		
		}
		
	}
	
	public void enemyAI(Sprite enemy)
	{
		
		xpTop = (int)(enemy.getX()/tmap.getTileWidth() + 0.6);
		ypTop = (int)((enemy.getY()+enemy.getHeight())/tmap.getTileHeight());
		
		xpLeft = (int)(enemy.getX()/tmap.getTileWidth() + 1);
		ypLeft = (int)((enemy.getY()+enemy.getHeight())/tmap.getTileHeight() - 0.75);
		
		xpRight = (int)(enemy.getX()/tmap.getTileWidth());
		ypRight = (int)((enemy.getY()+enemy.getHeight())/tmap.getTileHeight() - 0.5);
		

		
		if (tmap.getTileChar(xpTop, ypTop) == 'g' && enemy.getVelocityY() > 0)
		{
		
			enemy.setVelocityY(0);

		}
		
		if (tmap.getTileChar((xpLeft), ypLeft) == 'g' && enemy.getVelocityX() > 0)
		{
		
			enemy.setVelocityX(-0.05f);

		}
		
		if (tmap.getTileChar((xpRight), ypRight) == 'g' && enemy.getVelocityX() < 0)
		{
		
			enemy.setVelocityX(0.05f);
			
		}
		
	}
	
	public void loadMaps(){
		
		int i = 0;
		
		if(score > 1000 && loadedMap.equals("map1"))
		{
			
			tmap.loadMap("maps", "map2.txt");
			loadedMap = "map2";
			score = 0;
			
//			changeMaps(){
				
			player.setX(64);
			player.setY(48);
			player.setVelocityX(0);
			player.setVelocityY(0);
			
			
			
			for (Sprite en : enemies) 
			{

				en.setX(80 + (i*200));
				en.setY(40);
				i++;
			}
				
		}
		else if(score > 1000 && loadedMap.equals("map2"))
		{
			
			stop();
			
		}
		
		
		
	}
	
	/**
	 * Update any sprites and check for collisions
	 * 
	 * @param elapsed
	 *            The elapsed time between this call and the previous call of
	 *            elapsed
	 */
	public void update(long elapsed) {

		// Make adjustments to the speed of the sprite due to gravity
		player.setVelocityY(player.getVelocityY() + (gravity * elapsed * 4));
		player.setAnimationSpeed(1.0f);

		handlePCMovement();
		handleAttacking(player);
		checkTileCollision(player);
		handleTileMapCollisions(player, elapsed);
		checkScoreCollision(player);
		
		// Movement for left, right, down and jump
		if (player.getX() > (tmap.getMapWidth())) 
		{
			xo = xo + 5;
			tmap.draw(g, xo, yo);
		}

		for (Sprite s : backgroundShips)
			s.update(elapsed);

		for (Sprite en : enemies)
		{
			en.setVelocityY(en.getVelocityY() + (gravity * elapsed * 4));
			enemyAI(en);
			handlePlayerEnemyCollision(en);
			en.update(elapsed);
		}
		
		player.update(elapsed);
	
//		score = (int)player.getX();
		
		total = score;

	
		loadMaps();
		
	}
		

	/**
	 * Checks and handles collisions with the tile map for the given sprite 's'.
	 * Initial functionality is limited...
	 * 
	 * @param s
	 *            The Sprite to check collisions for
	 * @param elapsed
	 *            How time has gone by
	 */
	public void handleTileMapCollisions(Sprite s, long elapsed) {
		// This method should check actual tile map collisions. For
		// now it just checks if the player has gone off the bottom
		// of the tile map.

		if (player.getY() + player.getHeight() > tmap.getPixelHeight()) {

			// Put the player back on the map
			player.setY(tmap.getPixelHeight() - player.getHeight());
			player.resetJumps();

		}



		if (player.getX() < 0) {

			player.setX(0);

		}

		if (player.getX() > tmap.getPixelWidth() - player.getWidth()) {

			player.setX(tmap.getPixelWidth() - player.getWidth());

		}
	}

	public void checkTileCollision(Sprite sprite)
	{
		
		xpTop = (int)(sprite.getX()/tmap.getTileWidth() + 0.6);
		ypTop = (int)((sprite.getY()+sprite.getHeight())/tmap.getTileHeight());
		
		xpBottom = (int)(sprite.getX()/tmap.getTileWidth() + 0.5);
		ypBottom = (int)((sprite.getY()+sprite.getHeight())/tmap.getTileHeight() - 1.5);
		
		xpLeft = (int)(sprite.getX()/tmap.getTileWidth() + 1);
		ypLeft = (int)((sprite.getY()+sprite.getHeight())/tmap.getTileHeight() - 0.75);
		
		xpRight = (int)(sprite.getX()/tmap.getTileWidth());
		ypRight = (int)((sprite.getY()+sprite.getHeight())/tmap.getTileHeight() - 0.75);
		

		
		if (tmap.getTileChar(xpTop, ypTop) == 'g' && sprite.getVelocityY() > 0)
		{
			
			sprite.resetJumps();
			sprite.setVelocityY(0);
			handleDecel();

		}
		
		if (tmap.getTileChar((xpLeft), ypLeft) == 'g' && sprite.getVelocityX() > 0)
		{
		
			sprite.setVelocityX(0);

		}
		
		if (tmap.getTileChar((xpRight), ypRight) == 'g' && sprite.getVelocityX() < 0)
		{
		
			sprite.setVelocityX(0);

		}
	
		if (tmap.getTileChar(xpBottom, ypBottom) == 'g')
		{
			
			sprite.setVelocityY(0);
			sprite.shiftY(2);

		}
		
	}

	public void checkScoreCollision(Sprite player)
	{
		
		xpTop = (int)(player.getX()/tmap.getTileWidth() + 0.5);
		ypTop = (int)((player.getY()+player.getHeight())/tmap.getTileHeight());
		
		xpBottom = (int)(player.getX()/tmap.getTileWidth() + 0.5);
		ypBottom = (int)((player.getY()+player.getHeight())/tmap.getTileHeight() - 1);
		
		xpLeft = (int)(player.getX()/tmap.getTileWidth() + 1);
		ypLeft = (int)((player.getY()+player.getHeight())/tmap.getTileHeight() - 0.5);
		
		xpRight = (int)(player.getX()/tmap.getTileWidth());
		ypRight = (int)((player.getY()+player.getHeight())/tmap.getTileHeight() - 0.5);
		
		if(tmap.getTileChar((xpBottom), ypBottom) == 'b')
		{
			
			tmap.setTileChar('.', xpBottom, ypBottom);
			score += 100;
			
		}
		
		if(tmap.getTileChar((xpTop), ypTop) == 'b')
		{
			
			tmap.setTileChar('.', xpTop, ypTop);
			score += 100;
		}
		
		if(tmap.getTileChar((xpLeft), ypLeft) == 'b')
		{
			
			tmap.setTileChar('.', xpLeft, ypLeft);
			score += 100;
		}
		
		if(tmap.getTileChar((xpRight), ypRight) == 'b')
		{
			
			tmap.setTileChar('.', xpRight, ypRight);
			score += 100;
		}
		
		
		
	}
	
	/**
	 * Override of the keyPressed event defined in GameCore to catch our own
	 * events
	 * 
	 * @param e
	 *            The event that has been generated
	 */
	public void keyPressed(KeyEvent e){
		int key = e.getKeyCode();

		if (key == KeyEvent.VK_ESCAPE)
			stop();

		if (key == KeyEvent.VK_UP)
			moveJump = true;

		if (key == KeyEvent.VK_RIGHT)
		{
			if(!moveRight)
			{
			beforeTime = System.currentTimeMillis();
			}
			moveRight = true;
			
		}
		if (key == KeyEvent.VK_LEFT)
		{
			if(!moveLeft)
			{
			beforeTime = System.currentTimeMillis();
			}
			moveLeft = true;
		}
		if (key == KeyEvent.VK_DOWN)
			moveDown = true;

	}

	public void keyReleased(KeyEvent e){

		int key = e.getKeyCode();

		// Switch statement instead of lots of ifs...
		// Need to use break to prevent fall through.
		switch (key) {
		case KeyEvent.VK_ESCAPE:
			stop();
			break;
		case KeyEvent.VK_UP:
			moveJump = false;
			player.jumpUsed();
			player.toggleJumpReady();
			break;
		case KeyEvent.VK_RIGHT:
			
			
			moveRight = false;
			timeDiff = 0;

			
			break;
		case KeyEvent.VK_LEFT:
			
			moveLeft = false;
			timeDiff = 0;

			
			break;
		case KeyEvent.VK_DOWN:
			moveDown = false;
			break;
		default:
			break;
		}
	}
	
	public void mouseClicked(MouseEvent e)
	{
		
		int click = e.getButton();
		
		if(click == MouseEvent.BUTTON1)
		{
			
			if(!playerAttacking)
			{
			beforeAttack = System.currentTimeMillis();
			}

			playerAttacking = true;
			
			Sound s = new Sound("sounds/caw.wav");
			s.start();

		}

	}

}
