package com.brotato.player;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import com.almasb.fxgl.entity.components.ViewComponent;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameTimer;

import java.awt.*;


public class Player extends GameApplication {
    private Entity player;
    private AnimatedTexture texture;
    private AnimationChannel walkAnim;
    private AnimationChannel idleAnim;
    private AnimationChannel attackAnim;
    private int maxHP = 100;
    private int HP = 100;
    private Image fullHeart,halfHeart,emptyHeart;
    private Pane heartsPane;

    private boolean canAttack = true; // 攻击冷却标记

    private int movementKeyCount = 0;


    @Override
    protected void initSettings(GameSettings gameSettings) {
//        gameSettings.setWidth(800);
//        gameSettings.setHeight(600);
        gameSettings.setFullScreenFromStart(true);
        gameSettings.setTitle("Brotato");
        gameSettings.setVersion("0.1");
    }

    @Override
    protected void initGame() {


        //背景图片加入并保证在最下层
        FXGL.entityBuilder()
                .at(0,0)
                .view("background.png")
                .zIndex(-1)
                .buildAndAttach();

        // 读取精灵表 (假设每帧大小 128x128，共 7 帧)
        Image spriteSheet = FXGL.image("DruidWalk001-Sheet.png");
        walkAnim = new AnimationChannel(spriteSheet, 8, 128, 128, Duration.seconds(7.0 / 12.0), 0, 6);

        idleAnim = new AnimationChannel(spriteSheet, 8, 128, 128,
                Duration.seconds(1), 7, 7);

        //攻击（近战）动画
        Image attackSheet = FXGL.image("DruidBasicAtk1-Sheet.png");
        attackAnim = new AnimationChannel(attackSheet, 13, 128, 128,
                Duration.seconds(7.0 / 12.0), 0, 12);

        texture = new AnimatedTexture(idleAnim);

//        texture.loop();   // 默认循环播放

//        player = FXGL.entityBuilder()w
//                .at(400, 300)
//                .view(texture)
//                .buildAndAttach();


//        texture.stop(); // 初始静止

//        texture = new AnimatedTexture(idleAnim);

        texture.setTranslateX(-64); // 128像素宽 → 一半是64
        texture.setTranslateY(-64); // 128像素高 → 一半是64

        player = FXGL.entityBuilder()
                .at(400, 300)   // 玩家逻辑坐标
                .view(texture)  // 纹理居中在逻辑坐标
                .buildAndAttach();

        fullHeart = FXGL.image("heart.png");      // 满心
        halfHeart = FXGL.image("half_heart.png");  // 半心
        emptyHeart = FXGL.image("border.png"); // 空心（灰心）

        // ===== 创建 heartsPane =====
        heartsPane = new Pane();
        FXGL.addUINode(heartsPane, 20, 20);

        // 初始绘制血量条
        updateHearts();


        texture.playAnimationChannel(idleAnim);
    }

    private void updateHearts(){
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

    public void damage(int amount){
        HP = Math.max(0,HP - amount);
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
        FXGL.getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onActionBegin() {
                movementKeyCount++; // 按键数量+1
                if (movementKeyCount == 1) { // 首次按下时启动行走动画
                    texture.loopAnimationChannel(walkAnim);
                }
            }

            @Override
            protected void onAction() {
                if (player.getY() > 0) {
                    player.translateY(-1);
                }
            }

            @Override
            protected void onActionEnd() {
                movementKeyCount--;
                if (movementKeyCount == 0) { // 所有移动按键松开时才切换到静止
                    texture.playAnimationChannel(idleAnim);
                }// 回到站立帧
            }
        }, KeyCode.W);

        FXGL.getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onActionBegin() {
                movementKeyCount++;
                if (movementKeyCount == 1) {
                    texture.loopAnimationChannel(walkAnim);
                }
            }

            @Override
            protected void onAction() {
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

        // 向左
        FXGL.getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onActionBegin() {
                movementKeyCount++;
                if (movementKeyCount == 1) {
                    texture.loopAnimationChannel(walkAnim);
                }
                texture.setScaleX(-1); // 翻转朝左
            }

            @Override
            protected void onAction() {
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
                 // 保持面向左
            }
        }, KeyCode.A);

        // 向右
        FXGL.getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onActionBegin() {
                movementKeyCount++;
                if (movementKeyCount == 1) {
                    texture.loopAnimationChannel(walkAnim);
                }
                texture.setScaleX(1); // 正常朝右
            }

            @Override
            protected void onAction() {
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
                texture.setScaleX(1);
            }
        }, KeyCode.D);
        //按下空格键进行攻击，播放动画
        FXGL.getInput().addAction(new UserAction("Attack") {
            @Override
            protected void onActionBegin() {
                if (canAttack) {
                    canAttack = false;

                    // 记录攻击前的缩放方向（1为右，-1为左）
                    double scaleX = texture.getScaleX();
                    // 根据朝向向前移动32像素
                    player.translateX(23 * scaleX);

                    texture.playAnimationChannel(attackAnim);

                    // 攻击动画播完后回到 idle
                    texture.setOnCycleFinished(() -> {
                        texture.playAnimationChannel(idleAnim);
                        player.translateX(-23 * scaleX);
                        texture.setOnCycleFinished(() -> {}); // 清理回调
                    });

                    // 设置 2 秒冷却
                    FXGL.getGameTimer().runOnceAfter(() -> canAttack = true, Duration.seconds(2));
                }
            }
        }, KeyCode.SPACE);
    }


    public static void main(String[] args) {
        launch(args);
    }
}

