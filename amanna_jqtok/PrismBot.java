package amanna_jqtok;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import robocode.*;
import robocode.util.Utils;


public class PrismBot extends AdvancedRobot 
{
    static double enemyVelocity[][] = new double[400][4];
    static int currentEnemyVelocity;
    static int aimingEnemyVelocity;
    static double direction;
    static double turn = 2;
    int count;
    int averageCount;
    int turnDir = 1;
    int moveDir = 1;
    double velocityToAimAt;
    double time;
    double prevEnemyHeading;
    double prevEnergy = 100;
    boolean fired;
    public final double PERCENT_BUFFER =.20;

    public void run() 
    {
        setBodyColor(Color.yellow);
        setGunColor(Color.blue);
        setRadarColor(Color.red);
        setScanColor(Color.cyan);//the colors
        setAdjustGunForRobotTurn(true);//Sets the gun to turn independent from the robot's turn.
        setAdjustRadarForGunTurn(true);//same for radar
       
        while (true) //my robot is very dependent on when it scans a robot, but makes it easier to know what it is doing with one, hopefully
        {
        	turnRadarRightRadians(Double.POSITIVE_INFINITY);//keeps turning the radar right
        }
    }

    //what happens when you scan a robot
    public void onScannedRobot(ScannedRobotEvent e) 
    {
        if(e.getTime() % 6 == 0) {
	
        // Changes color every 6 turns
			
		setBodyColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
		setGunColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
		setRadarColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
		setBulletColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
		setScanColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	}
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        Graphics2D g = getGraphics();
        turn += 0.2 * Math.random();
        if (turn > 8) 
        {
            turn = 2;
        }

        if(prevEnergy - e.getEnergy() <= 3 && prevEnergy - e.getEnergy() >= 0.1) //getEnergy returns energy of robot, prevE is 100
        {
            if (Math.random() > .5) //unpredictable movement (hopefully)
            {
                turnDir *= -1;
            }
            if(Math.random() > .8) 
            {
                moveDir *= -1;
            }
        }

        setMaxTurnRate(turn);//sets maximum turn rate
        setMaxVelocity(12 - turn);
        setAhead(90 * moveDir);
        setTurnLeft(90 * turnDir);
        prevEnergy = e.getEnergy();

        if (e.getVelocity() < -2) //returns velocity of robot
        {
            currentEnemyVelocity = 0;
        }
        else if (e.getVelocity() > 2) 
        {
            currentEnemyVelocity = 1;
        }
        else if (e.getVelocity() <= 2 && e.getVelocity() >= -2) 
        {
            if (currentEnemyVelocity == 0) 
            {
                currentEnemyVelocity = 2;
            }
            else if (currentEnemyVelocity == 1) 
            {
                    currentEnemyVelocity = 3;
            }
        }//getTime gives us the gametime or matchtime
        if (getTime() - time > e.getDistance() / 12.8 && fired == true) //Returns the distance to the robot (your center to his center).
        {
            aimingEnemyVelocity=currentEnemyVelocity;//updates enemy's velocity
        }
        else 
        {
            fired = false;
        }

        enemyVelocity[count][aimingEnemyVelocity] = e.getVelocity();
        count++;////record a new enemy velocity and raise the count

        if(count == 400) //reset count
        {
            count = 0;
        }

        averageCount = 0;
        velocityToAimAt = 0;
        //calculate our average velocity for our current segment
        while(averageCount < 400) 
        {
            velocityToAimAt += enemyVelocity[averageCount][currentEnemyVelocity];
            averageCount++;
        }

        velocityToAimAt /= 400;

        double bulletPower = Math.min(2.3, Math.min(e.getEnergy()/3.5, getEnergy()/11));//determines bullet power, returns smaller value
        double myX = getX();//Returns the X position of my robot
        double myY = getY();//For Y position
        double enemyX = getX() + e.getDistance() * Math.sin(absBearing);//enemy
        double enemyY = getY() + e.getDistance() * Math.cos(absBearing);//coordinates
        double enemyHeading = e.getHeadingRadians();//heading of enemy
        double enemyHeadingChange = enemyHeading - prevEnemyHeading;
        prevEnemyHeading = enemyHeading;//updates enemy heading
        double counter = 0;
        double battleFieldHeight = getBattleFieldHeight();//self
        double battleFieldWidth = getBattleFieldWidth();//explanatory
        double predictedX = enemyX; 
        double predictedY = enemyY;
        //below is code to avoid walls, it cannot avoid the wall if it backs into it
        double xPos = this.getX();
        double yPos = this.getY();
        double width = this.getBattleFieldWidth();
        double height = this.getBattleFieldHeight();
        double buffer = PERCENT_BUFFER*Math.max(width, height);
        if(xPos < buffer)
        {
        	if((this.getHeading() < 90 && (this.getHeading() > 0)))
        	{
        		this.setTurnRight(180);
        	}
        	else if((this.getHeading() < 360) && (this.getHeading() > 270))
        	{
        		this.setTurnRight(180);
        	}
        }
        else if(xPos > width - buffer)
        {
        	if((this.getHeading() < 180) && (this.getHeading() > 90))
        	{
        		this.setTurnRight(90);
        	}
        	else if((this.getHeading() < 270) && (this.getHeading() > 180))
        	{
        		this.setTurnRight(90);
        	}
        }
        
        if(yPos < buffer)//too close to the bottom
        {
        	if((this.getHeading() < 180) && (this.getHeading() > 90))
        	{
        		this.setTurnLeft(90);//turn away from the floor
        	}
        	else if((this.getHeading() < 270) && (this.getHeading() > 180))
        	{
        		this.setTurnRight(90);
        	}
        }
        else if(yPos > height-buffer)//avoid ceiling
        {
        	if((this.getHeading() < 90) && (this.getHeading() > 0))
        	{
        		this.setTurnRight(90);
        	}
        	else if ((this.getHeading() < 360) && (this.getHeading() > 270))
        	{
        		this.setTurnLeft(90);
        	}
        }
        this.setAhead(10);
        this.execute();
        //above avoids walls, doesn't hug the walls though, so arena is smaller in a sense
        //predictive shooting, assume enemy is going to continue in the same direction and same speed to predict where to shoot
        //P2DD returns points between two points
        while ((++counter) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance( myX, myY, predictedX, predictedY))
        {      
            predictedX += Math.sin(enemyHeading) * velocityToAimAt;
            predictedY += Math.cos(enemyHeading) * velocityToAimAt;//predict where enemy is going to go and then shoots
            enemyHeading += enemyHeadingChange;
            g.setColor(Color.white);//paints where the robot is going to be
            g.fillOval((int)predictedX - 2,(int)predictedY - 2, 4, 4);

            if (predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0 || predictedY > battleFieldHeight - 18.0) 
            {   
                predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0); 
                predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
                break;
            }
        }
        
        double angle = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
        setTurnGunRightRadians(Utils.normalRelativeAngle(angle - getGunHeadingRadians()));

        if(getGunHeat() == 0) 
        {
            fire(bulletPower);
            fired = true;
        }  
        
        
    }//end of ScannedRobot
}