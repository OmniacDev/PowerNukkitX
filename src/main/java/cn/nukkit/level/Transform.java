package cn.nukkit.level;

import cn.nukkit.math.Vector3;
import cn.nukkit.utils.LevelException;
import org.jetbrains.annotations.NotNull;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class Transform extends LevelPosition {
    public double yaw;
    public double pitch;
    public double headYaw;

    public Transform() {
        this(0);
    }

    public Transform(double x) {
        this(x, 0);
    }

    public Transform(double x, double y) {
        this(x, y, 0);
    }

    public Transform(double x, double y, double z) {
        this(x, y, z, 0);
    }

    public Transform(double x, double y, double z, Level level) {
        this(x, y, z, 0, 0, level);
    }

    public Transform(double x, double y, double z, double yaw) {
        this(x, y, z, yaw, 0);
    }

    public Transform(double x, double y, double z, double yaw, double pitch) {
        this(x, y, z, yaw, pitch, null);
    }

    public Transform(double x, double y, double z, double yaw, double pitch, Level level) {
        this(x, y, z, yaw, pitch, 0, level);
    }

    public Transform(double x, double y, double z, double yaw, double pitch, double headYaw) {
        this(x, y, z, yaw, pitch, headYaw, null);
    }

    public Transform(double x, double y, double z, double yaw, double pitch, double headYaw, Level level) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.headYaw = headYaw;
        this.level = level;
    }

    public static Transform fromObject(Vector3 pos) {
        return fromObject(pos, null, 0.0f, 0.0f);
    }

    public static Transform fromObject(Vector3 pos, Level level) {
        return fromObject(pos, level, 0.0f, 0.0f);
    }

    public static Transform fromObject(Vector3 pos, Level level, double yaw) {
        return fromObject(pos, level, yaw, 0.0f);
    }

    public static Transform fromObject(Vector3 pos, Level level, double yaw, double pitch) {
        return new Transform(pos.x, pos.y, pos.z, yaw, pitch, (level == null) ? ((pos instanceof LevelPosition) ? ((LevelPosition) pos).level : null) : level);
    }

    public static Transform fromObject(Vector3 pos, Level level, double yaw, double pitch, double headYaw) {
        if (level == null && pos instanceof LevelPosition) {
            level = ((LevelPosition) pos).level;
        }
        return new Transform(pos.x, pos.y, pos.z, yaw, pitch, headYaw, level);
    }

    public double getYaw() {
        return this.yaw;
    }

    public Transform setYaw(double yaw) {
        this.yaw = yaw;
        return this;
    }

    public double getPitch() {
        return this.pitch;
    }

    public Transform setPitch(double pitch) {
        this.pitch = pitch;
        return this;
    }

    public double getHeadYaw() {
        return this.headYaw;
    }

    public Transform setHeadYaw(double headYaw) {
        this.headYaw = headYaw;
        return this;
    }

    @Override
    public Transform setX(double x) {
        super.setX(x);
        return this;
    }

    @Override
    public Transform setY(double y) {
        super.setY(y);
        return this;
    }

    @Override
    public Transform setZ(double z) {
        super.setZ(z);
        return this;
    }

    @Override
    public String toString() {
        return "Location (level=" + (this.isValid() ? this.getLevel().getName() : "null") + ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + ", headYaw=" + this.headYaw + ")";
    }

    @Override
    @NotNull public Transform getLocation() {
        if (this.isValid()) return new Transform(this.x, this.y, this.z, this.yaw, this.pitch, this.headYaw, this.level);
        else throw new LevelException("Undefined Level reference");
    }

    @Override
    public Transform add(double x) {
        return this.add(x, 0, 0);
    }

    @Override
    public Transform add(double x, double y) {
        return this.add(x, y, 0);
    }

    @Override
    public Transform add(double x, double y, double z) {
        return new Transform(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform add(Vector3 v) {
        return new Transform(this.x + v.getX(), this.y + v.getY(), this.z + v.getZ(), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform subtract(double x) {
        return this.subtract(x, 0, 0);
    }

    @Override
    public Transform subtract(double x, double y) {
        return this.subtract(x, y, 0);
    }

    @Override
    public Transform subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }

    @Override
    public Transform subtract(Vector3 v) {
        return this.add(-v.getX(), -v.getY(), -v.getZ());
    }

    @Override
    public Transform multiply(double number) {
        return new Transform(this.x * number, this.y * number, this.z * number, this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform divide(double number) {
        return new Transform(this.x / number, this.y / number, this.z / number, this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform ceil() {
        return new Transform((int) Math.ceil(this.x), (int) Math.ceil(this.y), (int) Math.ceil(this.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform floor() {
        return new Transform(this.getFloorX(), this.getFloorY(), this.getFloorZ(), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform round() {
        return new Transform(Math.round(this.x), Math.round(this.y), Math.round(this.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    @Override
    public Transform abs() {
        return new Transform((int) Math.abs(this.x), (int) Math.abs(this.y), (int) Math.abs(this.z), this.yaw, this.pitch, this.headYaw, this.level);
    }

    public Vector3 getDirectionVector() {
        double pitch = ((getPitch() + 90) * Math.PI) / 180;
        double yaw = ((getYaw() + 90) * Math.PI) / 180;
        double x = Math.sin(pitch) * Math.cos(yaw);
        double z = Math.sin(pitch) * Math.sin(yaw);
        double y = Math.cos(pitch);
        return new Vector3(x, y, z).normalize();
    }

    @Override
    public Transform clone() {
        return (Transform) super.clone();
    }
}
