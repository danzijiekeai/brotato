package com.brotato.player;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.ViewComponent;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import com.almasb.fxgl.input.UserAction;

public class Player extends GameApplication {
    private Entity player;
    private AnimatedTexture texture;
    private AnimationChannel walkAnim;
    private AnimationChannel idleAnim;
    private AnimationChannel attackAnim;
    private int maxHP = 100;
    private int HP = 100;
    private Image fullHeart, halfHeart, emptyHeart;
    private Pane heartsPane;

    private boolean canAttack = true; // 攻击冷却标记
    private boolean isAttacking = false;
    private int movementKeyCount = 0;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(1000);
        gameSettings.setHeight(800);
        gameSettings.setTitle("Brotato");
        gameSettings.setVersion("0.1");
    }

    @Override
    protected void initGame() {
        // 背景
        FXGL.entityBuilder()
                .at(0, 0)
                .view("background.png")
                .zIndex(-1)
                .buildAndAttach();

        // 行走动画
        Image spriteSheet = FXGL.image("DruidWalk001-Sheet.png");
        walkAnim = new AnimationChannel(spriteSheet, 8, 128, 128, Duration.seconds(7.0 / 12.0), 0, 6);
        idleAnim = new AnimationChannel(spriteSheet, 8, 128, 128, Duration.seconds(1), 7, 7);

        // 攻击动画
        Image attackSheet = FXGL.image("DruidBasicAtk1-Sheet.png");
        attackAnim = new AnimationChannel(attackSheet, 13, 128, 128, Duration.seconds(7.0 / 12.0), 0, 12);

        texture = new AnimatedTexture(idleAnim);
        texture.setTranslateX(-64);
        texture.setTranslateY(-64);

        // 玩家实体，带碰撞箱
        player = FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(400, 300)
                .view(texture)
                .bbox(new HitBox(new Point2D(32, 32), BoundingShape.box(64, 64))) // 碰撞箱居中
                .with(new CollidableComponent(true))
                .buildAndAttach();

        // 血量 UI
        fullHeart = FXGL.image("heart.png");
        halfHeart = FXGL.image("half_heart.png");
        emptyHeart = FXGL.image("border.png");

        heartsPane = new Pane();
        FXGL.getGameScene().addUINode(heartsPane);
        heartsPane.setTranslateX(20);
        heartsPane.setTranslateY(20);

        updateHearts();
        texture.playAnimationChannel(idleAnim);
    }

    // 碰撞检测
    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity player, Entity enemy) {
                damage(10);
                System.out.println("玩家被敌人撞击 -10HP");
            }
        });

        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.BULLET) {
            @Override
            protected void onCollisionBegin(Entity player, Entity bullet) {
                damage(5);
                bullet.removeFromWorld();
                System.out.println("玩家被子弹击中 -5HP");
            }
        });
    }

    private void updateHearts() {
        heartsPane.getChildren().clear();
        int hearts = maxHP / 10;
        int hpLeft = HP;

        for (int i = 0; i < hearts; i++) {
            ImageView heartView;
            if (hpLeft >= 10) {
                heartView = new ImageView(fullHeart);
                hpLeft -= 10;
            } else if (hpLeft >= 5) {
                heartView = new ImageView(halfHeart);
                hpLeft -= 5;
            } else {
                heartView = new ImageView(emptyHeart);
            }
            heartView.setFitWidth(32);
            heartView.setFitHeight(32);
            heartView.setTranslateX(i * 34);
            heartsPane.getChildren().add(heartView);
        }
    }

    public void damage(int amount) {
        HP = Math.max(0, HP - amount);
        updateHearts();
    }

    public void heal(int amount) {
        HP = Math.min(maxHP, HP + amount);
        updateHearts();
    }

    public void increaseMaxHP(int amount) {
        maxHP += amount;
        HP = maxHP;
        updateHearts();
    }

    @Override
    protected void initInput() {
        // 移动控制
        FXGL.getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onActionBegin() {
                //if (isAttacking) return;
                movementKeyCount++;
                if (movementKeyCount == 1) {
                    texture.loopAnimationChannel(walkAnim);
                }
            }

            @Override
            protected void onAction() {
                //if (isAttacking) return;
                if (player.getY() > 0) {
                    player.translateY(-1);
                }

            }

            @Override
            protected void onActionEnd() {
                movementKeyCount--;
                if (movementKeyCount == 0) {
                    texture.playAnimationChannel(idleAnim);
                }
            }
        }, KeyCode.W);

        FXGL.getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onActionBegin() {
                //if (isAttacking) return;
                movementKeyCount++;
                if (movementKeyCount == 1) {
                    texture.loopAnimationChannel(walkAnim);
                }
            }

            @Override
            protected void onAction() {
               // if (isAttacking) return;
                if (player.getBottomY() < FXGL.getAppHeight()) {
                    player.translateY(1);
                }

            }

            @Override
            protected void onActionEnd() {
                movementKeyCount--;
                if (movementKeyCount == 0) {
                    texture.playAnimationChannel(idleAnim);
                }
            }
        }, KeyCode.S);

        FXGL.getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onActionBegin() {
                //if (isAttacking) return;
                movementKeyCount++;
                if (movementKeyCount == 1) {
                    texture.loopAnimationChannel(walkAnim);
                }
                texture.setScaleX(-1);
            }

            @Override
            protected void onAction() {
                //if (isAttacking) return;
                if (player.getX() > 0) {
                    player.translateX(-1);
                }

            }

            @Override
            protected void onActionEnd() {
                movementKeyCount--;
                if (movementKeyCount == 0) {
                    texture.playAnimationChannel(idleAnim);
                }
            }
        }, KeyCode.A);

        FXGL.getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onActionBegin() {
                //if (isAttacking) return;
                movementKeyCount++;
                if (movementKeyCount == 1) {
                    texture.loopAnimationChannel(walkAnim);
                }
                texture.setScaleX(1);
            }

            @Override
            protected void onAction() {
                //if (isAttacking) return;
                if (player.getRightX() < FXGL.getAppWidth()) {
                    player.translateX(1);
                }

            }

            @Override
            protected void onActionEnd() {
                movementKeyCount--;
                if (movementKeyCount == 0) {
                    texture.playAnimationChannel(idleAnim);
                }
            }
        }, KeyCode.D);

        // 攻击
        FXGL.getInput().addAction(new UserAction("Attack") {
            @Override
            protected void onActionBegin() {

                if (canAttack) {
                    canAttack = false;
                    isAttacking = true;

                    double scaleX = texture.getScaleX();
                    player.translateX(23 * scaleX);
                    texture.playAnimationChannel(attackAnim);

                    texture.setOnCycleFinished(() -> {
                        texture.playAnimationChannel(idleAnim);
                        player.translateX(-23 * scaleX);

                        isAttacking = false;
                        texture.setOnCycleFinished(() -> {});
                    });

                    FXGL.getGameTimer().runOnceAfter(() -> canAttack = true, Duration.seconds(2));
                }
            }
        }, KeyCode.SPACE);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
