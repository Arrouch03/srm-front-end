package ma.srm.srm.frontend.network;

import java.util.List;

import ma.srm.srm.frontend.models.CompteurEau;
import ma.srm.srm.frontend.models.CompteurElectricite;
import ma.srm.srm.frontend.models.CompteurType;
import ma.srm.srm.frontend.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("users/login")
    Call<User> login(@Body User user);

    @GET("compteurs-eau")
    Call<List<CompteurEau>> getCompteursEau();

    @GET("compteurs-electricite")
    Call<List<CompteurElectricite>> getCompteursElectricite();

    @GET("compteur-types")
    Call<List<CompteurType>> getCompteurTypes();

    // Ajouter aussi le POST pour insérer un compteur
    @POST("compteurs-eau")
    Call<Void> createCompteurEau(@Body CompteurEau compteur);

    @POST("compteurs-electricite")
    Call<Void> createCompteurElectricite(@Body CompteurElectricite compteur);

    @PUT("compteurs-eau/{id}")
    Call<Void> updateCompteurEau(@Path("id") Long id, @Body CompteurEau compteur);

    @PUT("compteurs-electricite/{id}")
    Call<Void> updateCompteurElectricite(@Path("id") Long id, @Body CompteurElectricite compteur);

    // 📍 Mise à jour rapide de la position (latitude/longitude uniquement)
    @PATCH("compteurs-eau/{id}/position")
    Call<Void> updateCompteurEauPosition(@Path("id") Long id, @Body CompteurEau compteur);

    @PATCH("compteurs-electricite/{id}/position")
    Call<Void> updateCompteurElectricitePosition(@Path("id") Long id, @Body CompteurElectricite compteur);

}

