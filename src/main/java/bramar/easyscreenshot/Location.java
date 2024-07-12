package bramar.easyscreenshot;

import java.util.function.Function;

public class Location implements Cloneable {
	private final int x, y;
	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	@Override
	public Object clone() {
		return cloneLoc();
	}
	public Location cloneLoc() {
		return new Location(x, y);
	}
	public Location applyX(Function<Integer, Integer> x) {
		return new Location(x.apply(this.x), y);
	}
	public Location applyZ(Function<Integer, Integer> y) {
		return new Location(x, y.apply(this.y));
	}
	public Location applyPos(Function<Integer, Integer> x, Function<Integer, Integer> y) {
		int newX = x == null ? this.x : x.apply(this.x);
		int newZ = y == null ? this.y : y.apply(this.y);
		return new Location(newX, newZ);
	}
	public int distanceX(Location other) {
		int minX = Math.min(x, other.x);
		int maxX = Math.max(x, other.x);
		return maxX - minX;
	}
	public int distanceY(Location other) {
		int minY = Math.min(y, other.y);
		int maxY = Math.max(y, other.y);
		return maxY - minY;
	}
	public int distance(Location other) {
		return distanceX(other) + distanceY(other);
	}
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Location && distance((Location) obj) == 0;
	}
	@Override
	public String toString() {
		return "[" + x + ", " + y + "]";
	}
}
