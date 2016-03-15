/**
 * Stores a capture in a single direction.
 *
 * @author  T.Sergeant
 * @version for AI
 *
 * <p>We represent the direction of the capture by specifying the row offset
 * (ioff) and column offset (joff). The offsets can be +1, 0, or -1.</p>
 * <p>The value of n represents the number of pieces captures in the specified
 * direction.</p>
 * <pre>
 * ioff  joff   direction
 *  +1    +1    diag down-right
 *  +1    +0    down
 *  +1    -1    diag down-left
 *  +0    +1    right
 *  +0    -1    left
 *  -1    +1    diag up-right
 *  -1    +0    up
 *  -1    -1    diag up-left
 * </pre>
*/

public class Capture
{
	int ioff,joff;  // the direction in which pieces were captured
	int n;          // how many pieces were captured?

	/**
	 * Initialize the object.
	 *
	 * @param ioff the row-wise direction of the capture
	 * @param joff the column-wise direction of the capture
	 * @param n the number tiles turned over by this capture
	 */
	public Capture(int ioff, int joff, int n) {
		this.ioff= ioff;
		this.joff= joff;
		this.n= n;
	}
}
