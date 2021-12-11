package org.firstinspires.ftc.teamcode.old.lib;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.wheelerschool.robotics.old.lib.CompBot;

@Autonomous
public class DeadSimple extends OpMode {
    CompBot bot;

    @Override
    public void init() {
        bot = new CompBot(hardwareMap);
    }

    @Override
    public void loop() {
        bot.setDrive(0, 0, 0.25f);
    }
}
