//FOR INCLUDING
vec3 colorFunc(float value){
    vec3 colNoAlpha;
    if (value >= 0.0){ 
        float col = clamp( (value-minPosColor)/(maxPosColor-minPosColor) , 0.0, 1.0);
        if(colouring == 1){ //red
            float value = col;
            colNoAlpha = vec3(value, 0.0, 0.0); 
        }else if(colouring == 2){ //16 periods red
            float value = cos(2.0*PI*16.0*col)*0.5+0.5;
            colNoAlpha = vec3(value, 0.0, 0.0); 
        }else if(colouring == 3){ //cosine fire
            colNoAlpha = vec3(0.5) + cos( vec3(col*PI+PI) - vec3(-1.0,0.0,1.0) ); 
        }else{ //linear fire gradient
            colNoAlpha = vec3(col*3.0, col*3.0 - 1.0, col*3.0 - 2.0); 
        }
    }else{ 
        float col = clamp( (value-minNegColor)/(maxNegColor-minNegColor) , 0.0, 1.0);
        col = 1.0 - col;
        if(colouring == 1){ //blue
            float value = col;
            colNoAlpha = vec3(0.0, 0.0, value);
        }else if(colouring == 2){ //16 periods blue
            float value = cos(2.0*PI*16.0*col)*0.5+0.5;
            colNoAlpha = vec3(0.0, 0.0, value);
        }else if(colouring == 3){ //cosine ice
            colNoAlpha = vec3(0.5) + cos( vec3(col*PI+PI) - vec3(1.0,0.0,-1.0) ); 
        }else{ //linear ice gradient
            colNoAlpha = vec3(col*3.0 - 2.0, col*3.0 - 1.0, col*3.0); 
        }
    }

    return colNoAlpha;
}