package ma.srm.srm.frontend.network;

import java.util.List;

import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.models.CompteurElectricite;
import ma.srm.srm.frontend.models.CompteurType;
import ma.srm.srm.frontend.models.User;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.PATCH;
import retrofit2.http.DELETE;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    // 🔹 Authentification
    @POST("users/login")
    Call<User> login(@Body User user);

    // 🔹 Compteurs Eau
    @GET("compteurs-eau")
    Call<List<CompteurEau>> getCompteursEau();

    @GET("compteurs-eau/{id}")
    Call<CompteurEau> getCompteurEauById(@Path("id") Long id);

    @POST("compteurs-eau")
    Call<CompteurEau> createCompteurEau(@Body CompteurEau compteur);

    @PUT("compteurs-eau/{id}")
    Call<CompteurEau> updateCompteurEau(@Path("id") Long id, @Body CompteurEau compteur);

    @PATCH("compteurs-eau/{id}/position")
    Call<Void> updateCompteurEauPosition(@Path("id") Long id, @Body CompteurEau compteur);

    @DELETE("compteurs-eau/{id}")
    Call<Void> deleteCompteurEau(@Path("id") Long id);

    @Multipart
    @POST("compteurs-eau/{id}/photo")
    Call<Void> uploadCompteurEauPhoto(@Path("id") Long id,
                                      @Part MultipartBody.Part file);

    // 🔹 Compteurs Electricité
    @GET("compteurs-electricite")
    Call<List<CompteurElectricite>> getCompteursElectricite();

    @GET("compteurs-electricite/{id}")
    Call<CompteurElectricite> getCompteurElectriciteById(@Path("id") Long id);

    @POST("compteurs-electricite")
    Call<CompteurElectricite> createCompteurElectricite(@Body CompteurElectricite compteur);

    @PUT("compteurs-electricite/{id}")
    Call<CompteurElectricite> updateCompteurElectricite(@Path("id") Long id, @Body CompteurElectricite compteur);

    @PATCH("compteurs-electricite/{id}/position")
    Call<Void> updateCompteurElectricitePosition(@Path("id") Long id, @Body CompteurElectricite compteur);

    @DELETE("compteurs-electricite/{id}")
    Call<Void> deleteCompteurElectricite(@Path("id") Long id);

    @Multipart
    @POST("compteurs-electricite/{id}/photo")
    Call<Void> uploadCompteurElectricitePhoto(@Path("id") Long id,
                                              @Part MultipartBody.Part file);

    // 🔹 Compteur Types
    @GET("compteur-types")
    Call<List<CompteurType>> getCompteurTypes();
}
