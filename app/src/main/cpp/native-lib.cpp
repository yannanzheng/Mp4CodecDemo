#include <jni.h>
#include <string>
#include <malloc.h>

#ifdef __cplusplus
extern "C" {
#endif
#include "sei_packet.h"
#ifdef __cplusplus
}
#endif

jbyteArray getIMUData(JNIEnv *env, jbyteArray array) {
    jbyte *bytes = env->GetByteArrayElements(array, 0);
    uint8_t *packet_ = (uint8_t *) bytes;
    long len = env->GetArrayLength(array);
    if (packet_ == NULL || len == 0) {
        return NULL;
    }

    uint8_t *ret_data = NULL;
    uint32_t ret_size = 0;
    int ret = get_sei_content(packet_, len, IMU_UUID, &ret_data, &ret_size);
    if (ret != -1 && ret_data != NULL && ret_size > 0) {
        jbyteArray ret_array = env->NewByteArray(ret_size);
        jbyte *ret_byte = env->GetByteArrayElements(ret_array, 0);
        memcpy(ret_byte, ret_data, ret_size);
        free_sei_content(&ret_data);
        env->ReleaseByteArrayElements(ret_array, ret_byte, 0);
        return ret_array;
    }
    free_sei_content(&ret_data);
    if (packet_ != NULL && len > 4 && get_annexb_type(packet_, len) == 0) {
        uint8_t *packet_temp = (uint8_t *) malloc(len);
        if (packet_temp != NULL) {
            memcpy(packet_temp, packet_, len);
            packet_temp[0] = 0;
            packet_temp[1] = 0;
            packet_temp[2] = 0;
            packet_temp[3] = 1;
            ret = get_sei_content(packet_temp, len, IMU_UUID, &ret_data, &ret_size);
            free(packet_temp);
            packet_temp = NULL;
            if (ret != -1 && ret_data != NULL && ret_size > 0) {
                jbyteArray ret_array = env->NewByteArray(ret_size);
                jbyte *ret_byte = env->GetByteArrayElements(ret_array, 0);
                memcpy(ret_byte, ret_data, ret_size);
                free_sei_content(&ret_data);
                env->ReleaseByteArrayElements(ret_array, ret_byte, 0);

                if (bytes != NULL) {
                    env->ReleaseByteArrayElements(array, bytes, 0);
                }
                return ret_array;
            }
            free_sei_content(&ret_data);
        }
    }
    if (bytes != NULL) {
        env->ReleaseByteArrayElements(array, bytes, 0);
    }
    return NULL;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_jephy_mp4codecdemo_MainActivity_getIMUData(JNIEnv *env, jobject instance,
                                                    jbyteArray packet) {
    return getIMUData(env, packet);
}

extern "C"
jstring Java_com_jephy_mp4codecdemo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
