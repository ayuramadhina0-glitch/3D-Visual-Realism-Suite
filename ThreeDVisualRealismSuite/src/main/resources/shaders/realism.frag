#version 330 core
out vec4 FragColor;
in vec3 FragPos; in vec3 Normal; in vec2 TexCoords; in vec4 FragPosLightSpace;
uniform vec3 lightPos,viewPos,lightColor,objectColor;
uniform sampler2D diffuseTexture,shadowMap;
uniform int useBlinnPhong,flatShading,useTexture,useShadow;
float shadowCalculation(vec4 lightPosFrag,vec3 norm,vec3 lightDir){
 vec3 proj=lightPosFrag.xyz/lightPosFrag.w; proj=proj*0.5+0.5;
 if(proj.z>1.0)return 0.0;
 float closest=texture(shadowMap,proj.xy).r; float current=proj.z;
 float bias=max(0.005*(1.0-dot(norm,lightDir)),0.0008);
 float shadow=0.0; vec2 texel=1.0/textureSize(shadowMap,0);
 for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++){
  float p=texture(shadowMap,proj.xy+vec2(x,y)*texel).r;
  shadow+=(current-bias>p)?1.0:0.0;
 }
 return shadow/9.0;
}
void main(){
 vec3 norm=normalize(Normal);
 if(flatShading==1) norm=normalize(cross(dFdx(FragPos),dFdy(FragPos)));
 vec3 lightDir=normalize(lightPos-FragPos),viewDir=normalize(viewPos-FragPos);
 float ambientStrength=0.22; vec3 ambient=ambientStrength*lightColor;
 float diff=max(dot(norm,lightDir),0.0); vec3 diffuse=diff*lightColor;
 float spec=0.0;
 if(useBlinnPhong==1){vec3 halfwayDir=normalize(lightDir+viewDir);spec=pow(max(dot(norm,halfwayDir),0.0),32.0);}
 else {vec3 reflectDir=reflect(-lightDir,norm);spec=pow(max(dot(viewDir,reflectDir),0.0),16.0);}
 vec3 specular=0.65*spec*lightColor;
 float shadow=(useShadow==1)?shadowCalculation(FragPosLightSpace,norm,lightDir):0.0;
 vec3 base=(useTexture==1)?texture(diffuseTexture,TexCoords).rgb:objectColor;
 vec3 result=(ambient+(1.0-shadow)*(diffuse+specular))*base;
 FragColor=vec4(result,1.0);
}
