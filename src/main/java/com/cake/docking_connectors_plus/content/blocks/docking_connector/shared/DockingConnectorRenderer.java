package com.cake.docking_connectors_plus.content.blocks.docking_connector.shared;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public abstract class DockingConnectorRenderer<T extends SmartBlockEntity & DockingConnector> extends SafeBlockEntityRenderer<T> {

    protected DockingConnectorRenderer(final BlockEntityRendererProvider.Context context) {
    }

    protected abstract DockingConnectorModels getModels();

    @Override
    protected void renderSafe(final T be, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource, final int light, final int overlay) {
        final VertexConsumer vb = bufferSource.getBuffer(RenderType.cutout());
        final Direction direction = be.getBlockState().getValue(BlockStateProperties.FACING);
        final BlockState blockState = be.getBlockState();
        final float extension = be.getExtensionDistance(partialTicks);
        final float rotation = be.getFeetRotation(partialTicks) * 90;
        final DockingConnectorModels models = this.getModels();

        final SuperByteBuffer piston1 = CachedBuffers.partial(models.mainPistonBottom(), blockState);
        final SuperByteBuffer piston2 = CachedBuffers.partial(models.mainPistonTop(), blockState);
        final SuperByteBuffer sidePiston1 = CachedBuffers.partial(models.sidePistonBottom(), blockState);
        final SuperByteBuffer sidePiston2 = CachedBuffers.partial(models.sidePistonTop(), blockState);
        final SuperByteBuffer foot = CachedBuffers.partial(models.foot(), blockState);
        ms.pushPose();
        rotateToFaceCentered(ms, direction);
        piston1.translate(0, extension * 0.5, 0);
        piston2.translate(0, extension, 0);
        piston1.light(light).renderInto(ms, vb);
        piston2.light(light).renderInto(ms, vb);

        final Vector2f footAnchor = new Vector2f();
        final Vector2f sidePistonTopAnchor = new Vector2f();
        final Vector2f sidePistonBottomAnchor = new Vector2f();
        final Vector2f relativeAnchor = new Vector2f();

        footAnchor.set(-7.5f, 15.5f).div(16).add(0, extension);
        this.rotateVector2f(sidePistonTopAnchor.set(1.5f, -2.5f).div(16), rotation).add(footAnchor);
        sidePistonBottomAnchor.set(-6, 2).div(16).add(0, extension / 2);

        relativeAnchor.set(sidePistonTopAnchor).sub(sidePistonBottomAnchor);
        relativeAnchor.normalize();

        final Matrix4f rotationMatrix = new Matrix4f(
                1, 0, 0, 0,
                0, relativeAnchor.y, relativeAnchor.x, 0,
                0, -relativeAnchor.x, relativeAnchor.y, 0,
                0, 0, 0, 1
        );

        for (int i = 0; i < 4; i++) {
            ms.pushPose();
            ms.translate(0.5, 0, 0.5);
            TransformStack.of(ms).rotateYDegrees(i * 90);

            sidePiston1.translate(0, sidePistonBottomAnchor.y, sidePistonBottomAnchor.x);
            sidePiston2.translate(0, sidePistonTopAnchor.y, sidePistonTopAnchor.x);
            foot.translate(0, footAnchor.y, footAnchor.x);
            foot.rotateXDegrees(rotation);

            sidePiston1.mulPose(rotationMatrix);
            sidePiston2.mulPose(rotationMatrix);

            sidePiston1.light(light).renderInto(ms, vb);
            sidePiston2.light(light).renderInto(ms, vb);
            foot.light(light).renderInto(ms, vb);
            ms.popPose();
        }

        ms.popPose();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    public static void rotateToFaceCentered(final PoseStack ms, final Direction facing) {
        TransformStack.of(ms)
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
                .uncenter();
    }

    private Vector2f rotateVector2f(final Vector2f v, float angle) {
        angle = (float) Math.toRadians(angle);
        final float s = Mth.sin(angle);
        final float c = Mth.cos(angle);
        v.set(v.x * c + v.y * s, v.y * c - v.x * s);
        return v;
    }
}
