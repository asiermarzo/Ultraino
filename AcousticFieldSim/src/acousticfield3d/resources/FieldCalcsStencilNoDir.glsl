//FOR INCLUDING
vec2 fieldAt(vec3 point){
    vec2 field = vec2(0.0);

    for(int i = 0; i < N_TRANS; ++i){ //try loop unroll
        vec3 diffVec = point - tPos[i];
        
        float dist = length(diffVec);

        float ampDirAtt = tSpecs[i].y / dist;
        float kdPlusPhase = tSpecs[i].x * dist + tSpecs[i].z;
        field.x += ampDirAtt * cos(kdPlusPhase);
        field.y += ampDirAtt * sin(kdPlusPhase);
    }

    return field;
}
