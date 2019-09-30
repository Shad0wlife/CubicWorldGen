/*
 *  This file is part of Cubic World Generation, licensed under the MIT License (MIT).
 *
 *  Copyright (c) 2015 contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.structure.feature;

import com.google.common.collect.Lists;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorld;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.feature.CubicFeatureGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.feature.ICubicFeatureStart;
import io.github.opencubicchunks.cubicchunks.cubicgen.CustomCubicMod;
import io.github.opencubicchunks.cubicchunks.cubicgen.customcubic.CustomGeneratorSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.github.opencubicchunks.cubicchunks.api.util.Coords.cubeToCenterBlock;

public class CubicVillageGenerator extends CubicFeatureGenerator {

    private int distance;
    private final int minTownSeparation;
    private int size;
    private List<Biome> allowedBiomes = MapGenVillage.VILLAGE_SPAWN_BIOMES;
    private boolean positionsGenerated;
    private final CustomGeneratorSettings conf;

    public CubicVillageGenerator(CustomGeneratorSettings conf){
        super(2, 0);

        this.conf = conf;
        this.distance = 32;
        this.minTownSeparation = 8;
    }

    public CubicVillageGenerator(CustomGeneratorSettings conf, Map<String, String> data) {
        this(conf);

        for (Map.Entry<String, String> entry : data.entrySet()) {
            switch (entry.getKey()) {
                case "distance":
                    this.distance = MathHelper.getInt(entry.getValue(), this.distance, 9);
                    break;
                case "size":
                    this.size = MathHelper.getInt(entry.getValue(), this.size, 0);
                    break;
            }
        }
    }

    @Override
    protected boolean canSpawnStructureAtCoords(World world, Random random, int chunkX, int chunkY, int chunkZ) {
        int i = chunkX;
        int j = chunkZ;

        /*
        if((int)(conf.expectedBaseHeight - conf.expectedHeightVariation) - 16 < chunkY * 16 || (int)(conf.expectedBaseHeight + conf.expectedHeightVariation) + 16 > chunkY * 16){
            return false;
        }
        */

        if (chunkX < 0)
        {
            chunkX -= this.distance - 1;
        }

        if (chunkZ < 0)
        {
            chunkZ -= this.distance - 1;
        }

        int k = chunkX / this.distance;
        int l = chunkZ / this.distance;
        Random randomSeeded = world.setRandomSeed(k, l, 10387312); //Is this overwriting behaviour ok?
        k = k * this.distance;
        l = l * this.distance;
        k = k + randomSeeded.nextInt(this.distance - 8);
        l = l + randomSeeded.nextInt(this.distance - 8);

        if (i == k && j == l)
        {
            boolean flag = world.getBiomeProvider().areBiomesViable(i * 16 + 8, j * 16 + 8, 0, allowedBiomes);

            if (flag)
            {
                CustomCubicMod.LOGGER.info("Viable Village Spot at (" + (chunkX * 16) + ", " + (chunkY * 16) + ", " + (chunkZ * 16) + ").");
                return true;
            }
        }

        return false;
    }

    @Override
    protected StructureStart getStructureStart(World world, Random random, int chunkX, int chunkY, int chunkZ) {
        CustomCubicMod.LOGGER.info("Fetched Village Start at (" + (chunkX * 16) + ", " + (chunkZ * 16) + ").");
        return new MapGenVillage.Start(world, random, chunkX, chunkZ, this.size);
    }

    @Override
    public String getStructureName()
    {
        return "Village";
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World world, BlockPos blockPos, boolean findUnexplored) {
        return findNearestStructurePosBySpacing(world, blockPos, this.distance, 8, 10387312, false, 100, findUnexplored);
    }

    //Minimum effort - probably breaks
    //TODO fix for 3D
    protected BlockPos findNearestStructurePosBySpacing(World worldIn, BlockPos startPos, int distanceStep, int stepOffset, int randomSeedZ, boolean addExtraRandomness, int maxAttempts, boolean findUnexplored)
    {
        int i = startPos.getX() >> 4;
        int j = startPos.getZ() >> 4;
        int ypos = startPos.getY() >> 4;
        int k = 0;

        for (Random random = new Random(); k <= maxAttempts; ++k)
        {
            for (int l = -k; l <= k; ++l)
            {
                boolean flag = l == -k || l == k;

                for (int i1 = -k; i1 <= k; ++i1)
                {
                    boolean flag1 = i1 == -k || i1 == k;

                    if (flag || flag1)
                    {
                        int j1 = i + distanceStep * l;
                        int k1 = j + distanceStep * i1;

                        if (j1 < 0)
                        {
                            j1 -= distanceStep - 1;
                        }

                        if (k1 < 0)
                        {
                            k1 -= distanceStep - 1;
                        }

                        int l1 = j1 / distanceStep;
                        int i2 = k1 / distanceStep;
                        Random random1 = worldIn.setRandomSeed(l1, i2, randomSeedZ);
                        l1 = l1 * distanceStep;
                        i2 = i2 * distanceStep;

                        if (addExtraRandomness)
                        {
                            l1 = l1 + (random1.nextInt(distanceStep - stepOffset) + random1.nextInt(distanceStep - stepOffset)) / 2;
                            i2 = i2 + (random1.nextInt(distanceStep - stepOffset) + random1.nextInt(distanceStep - stepOffset)) / 2;
                        }
                        else
                        {
                            l1 = l1 + random1.nextInt(distanceStep - stepOffset);
                            i2 = i2 + random1.nextInt(distanceStep - stepOffset);
                        }

                        MapGenBase.setupChunkSeed(worldIn.getSeed(), random, l1, i2);
                        random.nextInt();

                        if (canSpawnStructureAtCoords(worldIn, random1, l1, ypos, i2)) //chunkY is ignored anyways for now
                        {
                            if (!findUnexplored || !worldIn.isChunkGeneratedAt(l1, i2))
                            {
                                return new BlockPos((l1 << 4) + 8, 64, (i2 << 4) + 8);
                            }
                        }
                        else if (k == 0)
                        {
                            break;
                        }
                    }
                }

                if (k == 0)
                {
                    break;
                }
            }
        }

        return null;
    }


    @Override public synchronized boolean generateStructure(World world, Random rand, CubePos cubePos) {
        this.initializeStructureData(world);
        int centerX = cubeToCenterBlock(cubePos.getX());
        int centerY = cubeToCenterBlock(cubePos.getY());
        int centerZ = cubeToCenterBlock(cubePos.getZ());
        boolean generated = false;

        if(((ICubicWorld)world).getSurfaceForCube(cubePos, 8, 8, 0, ICubicWorld.SurfaceType.SOLID) == null){
            return generated;
        }

        for (ICubicFeatureStart cubicStructureStart : this.structureMap) {
            StructureStart structStart = (StructureStart) cubicStructureStart;
            // TODO: cubic chunks version of isValidForPostProcess and notifyPostProcess (mixin)
            if (structStart.isSizeableStructure() && structStart.isValidForPostProcess(cubePos.chunkPos())
                    && structStart.getBoundingBox().intersectsWith(
                    new StructureBoundingBox(centerX, centerY, centerZ, centerX + ICube.SIZE - 1, centerY + ICube.SIZE - 1, centerZ + ICube.SIZE - 1))) {
                structStart.generateStructure(world, rand,
                        new StructureBoundingBox(centerX, centerY, centerZ, centerX + ICube.SIZE - 1, centerY + ICube.SIZE - 1, centerZ + ICube.SIZE - 1));
                structStart.notifyPostProcessAt(cubePos.chunkPos());
                generated = true;
                this.setStructureStart(structStart.getChunkPosX(), cubicStructureStart.getChunkPosY(), structStart.getChunkPosZ(), structStart);
            }
        }

        return generated;
    }


    public interface CubicStart extends ICubicFeatureStart{
        void initCubicVillage(World world, int cubeY);
    }
}
