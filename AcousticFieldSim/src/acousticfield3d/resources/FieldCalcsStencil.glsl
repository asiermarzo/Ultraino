//BEGIN FIELD
vec2 fieldAt(vec3 point){
    vec2 field = vec2(0.0);

    for(int i = 0; i < N_TRANS; ++i){ //try loop unroll
        vec3 diffVec = point - tPos[i];
        
        float dist = length(diffVec);

        float angle = acos( dot(diffVec, tNorm[i]) / dist);

        float dum = tSpecs[i].x * 0.5 * tSpecs[i].w * sin( angle );
        float directivity;
        if(dum == 0.0){
            directivity = 1.0;
        }else{
            directivity = sin(dum) / dum;
        }

        float ampDirAtt = tSpecs[i].y * directivity / dist;
        float kdPlusPhase = tSpecs[i].x * dist + tSpecs[i].z;
        field.x += ampDirAtt * cos(kdPlusPhase);
        field.y += ampDirAtt * sin(kdPlusPhase);
    }

    return field;
}
//END
