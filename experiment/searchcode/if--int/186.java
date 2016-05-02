package com.jphysx;

/**
 * Copyright (c) 2007-2008, Yuri Kravchik and AGEIA
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the Yuri Kravchik nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
public final class NxParameter {
  public final static int NX_PENALTY_FORCE = 0;
  public final static int NX_SKIN_WIDTH = 1;
  public final static int NX_DEFAULT_SLEEP_LIN_VEL_SQUARED = 2;
  public final static int NX_DEFAULT_SLEEP_ANG_VEL_SQUARED = 3;
  public final static int NX_BOUNCE_THRESHOLD = 4;
  public final static int NX_DYN_FRICT_SCALING = 5;
  public final static int NX_STA_FRICT_SCALING = 6;
  public final static int NX_MAX_ANGULAR_VELOCITY = 7;
  public final static int NX_CONTINUOUS_CD = 8;
  public final static int NX_VISUALIZATION_SCALE = 9;
  public final static int NX_VISUALIZE_WORLD_AXES = 10;
  public final static int NX_VISUALIZE_BODY_AXES = 11;
  public final static int NX_VISUALIZE_BODY_MASS_AXES = 12;
  public final static int NX_VISUALIZE_BODY_LIN_VELOCITY = 13;
  public final static int NX_VISUALIZE_BODY_ANG_VELOCITY = 14;
  public final static int NX_VISUALIZE_BODY_JOINT_GROUPS = 22;
  public final static int NX_VISUALIZE_JOINT_LOCAL_AXES = 27;
  public final static int NX_VISUALIZE_JOINT_WORLD_AXES = 28;
  public final static int NX_VISUALIZE_JOINT_LIMITS = 29;
  public final static int NX_VISUALIZE_CONTACT_POINT = 33;
  public final static int NX_VISUALIZE_CONTACT_NORMAL = 34;
  public final static int NX_VISUALIZE_CONTACT_ERROR = 35;
  public final static int NX_VISUALIZE_CONTACT_FORCE = 36;
  public final static int NX_VISUALIZE_ACTOR_AXES = 37;
  public final static int NX_VISUALIZE_COLLISION_AABBS = 38;
  public final static int NX_VISUALIZE_COLLISION_SHAPES = 39;
  public final static int NX_VISUALIZE_COLLISION_AXES = 40;
  public final static int NX_VISUALIZE_COLLISION_COMPOUNDS = 41;
  public final static int NX_VISUALIZE_COLLISION_VNORMALS = 42;
  public final static int NX_VISUALIZE_COLLISION_FNORMALS = 43;
  public final static int NX_VISUALIZE_COLLISION_EDGES = 44;
  public final static int NX_VISUALIZE_COLLISION_SPHERES = 45;
  public final static int NX_VISUALIZE_COLLISION_STATIC = 47;
  public final static int NX_VISUALIZE_COLLISION_DYNAMIC = 48;
  public final static int NX_VISUALIZE_COLLISION_FREE = 49;
  public final static int NX_VISUALIZE_COLLISION_CCD = 50;
  public final static int NX_VISUALIZE_COLLISION_SKELETONS = 51;
  public final static int NX_VISUALIZE_FLUID_EMITTERS = 52;
  public final static int NX_VISUALIZE_FLUID_POSITION = 53;
  public final static int NX_VISUALIZE_FLUID_VELOCITY = 54;
  public final static int NX_VISUALIZE_FLUID_KERNEL_RADIUS = 55;
  public final static int NX_VISUALIZE_FLUID_BOUNDS = 56;
  public final static int NX_VISUALIZE_FLUID_PACKETS = 57;
  public final static int NX_VISUALIZE_FLUID_MOTION_LIMIT = 58;
  public final static int NX_VISUALIZE_FLUID_DYN_COLLISION = 59;
  public final static int NX_VISUALIZE_FLUID_STC_COLLISION = 60;
  public final static int NX_VISUALIZE_FLUID_MESH_PACKETS = 61;
  public final static int NX_VISUALIZE_FLUID_DRAINS = 62;
  public final static int NX_VISUALIZE_FLUID_PACKET_DATA = 90;
  public final static int NX_VISUALIZE_CLOTH_MESH = 63;
  public final static int NX_VISUALIZE_CLOTH_COLLISIONS = 64;
  public final static int NX_VISUALIZE_CLOTH_SELFCOLLISIONS = 65;
  public final static int NX_VISUALIZE_CLOTH_WORKPACKETS = 66;
  public final static int NX_VISUALIZE_CLOTH_SLEEP = 67;
  public final static int NX_VISUALIZE_CLOTH_SLEEP_VERTEX = 94;
  public final static int NX_VISUALIZE_CLOTH_TEARABLE_VERTICES = 80;
  public final static int NX_VISUALIZE_CLOTH_TEARING = 81;
  public final static int NX_VISUALIZE_CLOTH_ATTACHMENT = 82;
  public final static int NX_VISUALIZE_CLOTH_VALIDBOUNDS = 92;
  public final static int NX_VISUALIZE_SOFTBODY_MESH = 83;
  public final static int NX_VISUALIZE_SOFTBODY_COLLISIONS = 84;
  public final static int NX_VISUALIZE_SOFTBODY_WORKPACKETS = 85;
  public final static int NX_VISUALIZE_SOFTBODY_SLEEP = 86;
  public final static int NX_VISUALIZE_SOFTBODY_SLEEP_VERTEX = 95;
  public final static int NX_VISUALIZE_SOFTBODY_TEARABLE_VERTICES = 87;
  public final static int NX_VISUALIZE_SOFTBODY_TEARING = 88;
  public final static int NX_VISUALIZE_SOFTBODY_ATTACHMENT = 89;
  public final static int NX_VISUALIZE_SOFTBODY_VALIDBOUNDS = 93;
  public final static int NX_ADAPTIVE_FORCE = 68;
  public final static int NX_COLL_VETO_JOINTED = 69;
  public final static int NX_TRIGGER_TRIGGER_CALLBACK = 70;
  public final static int NX_SELECT_HW_ALGO = 71;
  public final static int NX_VISUALIZE_ACTIVE_VERTICES = 72;
  public final static int NX_CCD_EPSILON = 73;
  public final static int NX_SOLVER_CONVERGENCE_THRESHOLD = 74;
  public final static int NX_BBOX_NOISE_LEVEL = 75;
  public final static int NX_IMPLICIT_SWEEP_CACHE_SIZE = 76;
  public final static int NX_DEFAULT_SLEEP_ENERGY = 77;
  public final static int NX_CONSTANT_FLUID_MAX_PACKETS = 78;
  public final static int NX_CONSTANT_FLUID_MAX_PARTICLES_PER_STEP = 79;
  public final static int NX_VISUALIZE_FORCE_FIELDS = 91;
  public final static int NX_ASYNCHRONOUS_MESH_CREATION = 96;
  public final static int NX_FORCE_FIELD_CUSTOM_KERNEL_EPSILON = 97;
  public final static int NX_PARAMS_NUM_VALUES = 98;
  public final static int NX_PARAMS_FORCE_DWORD = 0x7fffffff;
}


